package edu.sysu.ncps.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mura.json.JSONArray;
import org.mura.json.JSONBoolean;
import org.mura.json.JSONData;
import org.mura.json.JSONFloat;
import org.mura.json.JSONInteger;
import org.mura.json.JSONObject;
import org.mura.json.JSONString;
import org.mura.json.JSONVariable;

public abstract class BaseServlet<P, J> extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * 是否使用浏览器调试标识位(影响返回的格式)
	 */
	private static final boolean BROWSER_DEBUG = true;

	// 用于更清晰地在错误信息中类型名, 设置一组类型名和Java类型的映射
	public static final Map<Class<?>, String> TYPE_DESCRIPTION = new HashMap<Class<?>, String>() {

		private static final long serialVersionUID = 1L;

		{
			put(Integer.class, "Integer");
			put(Float.class, "Float");
			put(Boolean.class, "Bool");
			put(String.class, "String");
			put(byte[].class, "Data");
			put(List.class, "Array");
			put(Map.class, "Object");
		}
	};

	/**
	 * 处理请求的主方法
	 * 
	 * @param paraBean
	 *            请求参数Bean, 需继承自BaseParaBean
	 * @param jsonBean
	 *            JSON格式Bean, 需继承自BaseJSONBean
	 * @throws Exception
	 *             如果处理过程出现异常, 抛出异常中断请求处理
	 */
	abstract protected void main(P para, J json) throws Exception;

	/**
	 * 处理异常, 并返回错误信息<br>
	 * 默认的处理方法是打印至控制台和返回页面
	 * 
	 * @param out
	 *            PrintWriter对象
	 * @param e
	 *            异常对象
	 */
	protected void handleException(PrintWriter out, Exception e) {
		e.printStackTrace();
		e.printStackTrace(out);
	}

	/**
	 * doGet, 不直接使用此方法
	 */
	final protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGetOrPost(request, response);
	}

	/**
	 * doPost, 不直接使用此方法
	 */
	final protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGetOrPost(request, response);
	}

	/**
	 * 处理GET请求和POST请求
	 * 
	 * @param request
	 *            请求对象
	 * @param response
	 *            应答对象
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void doGetOrPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 设置编码格式
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		if (BROWSER_DEBUG) {
			response.setContentType("text/plain");
		} else {
			response.setContentType("application/json");
		}

		// 获取PrintWriter对象
		PrintWriter out = response.getWriter();

		try {
			// 创建输出JSON对象
			JSONObject json = new JSONObject(null);

			// 创建参数打包的对象
			P paraBean = (P) createParaBean();
			assignParaBean(request, paraBean);
			J jsonBean = (J) createJSONBean();

			// 调用主方法处理请求
			main(paraBean, jsonBean);

			// 返回输出JSON对象生成的字符串
			assignJSONObject(jsonBean, json);
			if (BROWSER_DEBUG) {
				out.print(json.toFormatString());
			} else {
				out.print(json.toString());
			}

		} catch (Exception e) {
			// 捕获并处理异常, 返回相应信息
			handleException(out, e);
		}

	}

	/**
	 * 请求参数Bean基类, 继承后使用<br>
	 * 需要在Servlet的子类中自定义一个静态内部类, 必须继承自此类<br>
	 * 需要定义为<b>public</b>不然不能被反射获取<br>
	 * 只要定义一个, 即使定义多个也只有一个会起作用
	 * <p>
	 * 在子类中, 需要用成员变量定义从客户端发来的参数及其类型, 变量格式如下:<br>
	 * 访问标识: public<br>
	 * 变量名: 同请求参数的键名<br>
	 * 若为可选参数, 在键名前加<b>'_'</b>作为前缀以示区分<br>
	 * 在请求中出现了而在<b>Servlet</b>没有处理的参数, 不会影响执行, 不能被获取(想要获取, 请设置为可选变量)
	 * <p>
	 * 类型关系表: (JSON -> Java)<br>
	 * Integer -> <b>Integer</b><br>
	 * Float -> <b>Float</b><br>
	 * Bool -> <b>Boolean</b><br>
	 * String -> <b>String</b><br>
	 * Data -> <b>byte[]</b><br>
	 * &lt;Any&gt;Array -> <b>List&lt;String&gt;</b><br>
	 * (!)由于泛型限制, 数组只能保存字符串值, 需要取出后自行进行类型转换<br>
	 * 定义其它类型的成员变量可能导致问题!
	 * <p>
	 * 变量命名举例:<br>
	 * <b>public String</b> comment;<br>
	 * 对应<b>String</b>类型的名为<b>comment</b>的必要参数<br>
	 * <b>public byte[]</b> _image;<br>
	 * 对应<b>Data</b>类型的名为<b>image</b>的可选参数
	 * 
	 * @author mura
	 */
	public static class BaseParaBean {

		/**
		 * 将字符串List转换为整型List
		 * 
		 * @param list
		 *            字符串List
		 * @return 整型List
		 */
		public static List<Integer> convertIntegerList(List<String> list) {
			List<Integer> tList = new ArrayList<Integer>();
			for (String str : list) {
				tList.add(Integer.parseInt(str));
			}
			return tList;
		}

		/**
		 * 将字符串List转换为浮点型List
		 * 
		 * @param list
		 *            字符串List
		 * @return 浮点型List
		 */
		public static List<Float> convertFloatList(List<String> list) {
			List<Float> tList = new ArrayList<Float>();
			for (String str : list) {
				tList.add(Float.parseFloat(str));
			}
			return tList;
		}

		/**
		 * 将字符串List转换为布尔型List
		 * 
		 * @param list
		 *            字符串List
		 * @return 布尔型List
		 */
		public static List<Boolean> convertBoolList(List<String> list) {
			List<Boolean> tList = new ArrayList<Boolean>();
			for (String str : list) {
				tList.add(Boolean.parseBoolean(str));
			}
			return tList;
		}

		/**
		 * 将字符串List转换为二进制型List
		 * 
		 * @param list
		 *            字符串List
		 * @return 二进制型List
		 */
		public static List<byte[]> convertDataList(List<String> list) {
			List<byte[]> tList = new ArrayList<byte[]>();
			for (String str : list) {
				tList.add(Base64.getDecoder().decode(str));
			}
			return tList;
		}
	}

	/**
	 * 生成一个请求参数Bean
	 * 
	 * @throws Exception
	 * @author mura
	 */
	private BaseParaBean createParaBean() throws Exception {
		Class<?>[] classes = this.getClass().getClasses();
		for (Class<?> clazz : classes) {
			if (BaseParaBean.class.isAssignableFrom(clazz)) {
				BaseParaBean bean = (BaseParaBean) clazz.newInstance();
				return bean;
			}
		}
		return null;
	}

	/**
	 * 为参数Bean填充赋值
	 * 
	 * @param request
	 *            Http请求对象
	 * @param bean
	 *            请求参数Bean
	 * @throws Exception
	 */
	private void assignParaBean(HttpServletRequest request, P bean) throws Exception {

		// 获取请求参数Map
		Map<String, String[]> paraMap = request.getParameterMap();

		// 获取成员变量数组并遍历
		Field[] fields = bean.getClass().getFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			if (fieldName.charAt(0) != '_') {
				// 必要参数
				if (!paraMap.containsKey(fieldName)) {
					// 必要参数不存在, 返回错误信息
					throw new InvalidBeanFieldTypeException("Missing required parameter: " + fieldName + ", type: "
							+ TYPE_DESCRIPTION.get(field.getType()));
				}
			} else {
				// 可选参数, 如果不存在就跳过
				fieldName = fieldName.substring(1);
				if (!paraMap.containsKey(fieldName)) {
					continue;
				}
			}

			// 存在此参数, 根据Bean的类型进行赋值
			Class<?> fieldType = field.getType();
			if (List.class.isAssignableFrom(fieldType)) {
				// Bean中规定此值为数组, 根据数组进行赋值
				List<String> list = new ArrayList<String>();
				for (String value : paraMap.get(fieldName)) {
					list.add(value);
				}
				field.set(bean, list);
			} else {
				// 此值不为数组, 直接赋第一个值
				String value = paraMap.get(fieldName)[0];
				field.set(bean, parseParaValue(value, fieldType));
			}
		}
	}

	/**
	 * 将字符串形式的请求参数分析为特定的类型
	 * 
	 * @param value
	 *            字符串参数
	 * @param type
	 *            类型
	 * @return 分析后的Object
	 * @throws InvalidBeanFieldTypeException
	 *             由于Bean的类型不正确导致的异常
	 */
	private Object parseParaValue(String value, Class<?> type) throws InvalidBeanFieldTypeException {
		try {
			if (type.equals(Integer.class)) {
				return Integer.parseInt(value);
			} else if (type.equals(Float.class)) {
				return Float.parseFloat(value);
			} else if (type.equals(Boolean.class)) {
				return Boolean.parseBoolean(value);
			} else if (type.equals(String.class)) {
				return value;
			} else if (type.equals(byte[].class)) {
				return Base64.getDecoder().decode(value);
			} else {
				throw new InvalidBeanFieldTypeException("Invalid ParaBean field type: " + TYPE_DESCRIPTION.get(type));
			}
		} catch (IllegalArgumentException e) {
			throw new InvalidBeanFieldTypeException("Cannot parse: " + value + " into " + TYPE_DESCRIPTION.get(type));
		}
	}

	/**
	 * JSON格式Bean基类, 继承后使用<br>
	 * 需要在Servlet的子类中自定义一个静态内部类, 必须继承自此类<br>
	 * 需要定义为<b>public</b>不然不能被反射获取<br>
	 * 只要定义一个, 即使定义多个也只有一个会起作用
	 * <p>
	 * 在子类中, 需要用成员变量定义将要返回的JSON键值对及其类型, 变量格式如下:<br>
	 * 访问标识: public<br>
	 * 变量名: 同键值对的键名<br>
	 * 若为可选键值对, 在键名前加<b>'_'</b>作为前缀以示区分<br>
	 * 必要键值对将一定被返回, 如果没有赋值时将会返回<b>null</b>, 可选键值对只在有值时返回
	 * <p>
	 * 类型关系表: (JSON -> Java)<br>
	 * Integer -> <b>Integer</b><br>
	 * Float -> <b>Float</b><br>
	 * Bool -> <b>Boolean</b><br>
	 * String -> <b>String</b><br>
	 * Data -> <b>byte[]</b><br>
	 * &lt;Any&gt;Array -> <b>List&lt;(上述类型)&gt;</b><br>
	 * 定义其它类型的成员变量可能导致问题!
	 * <p>
	 * 变量命名举例:<br>
	 * <b>private String</b> comment;<br>
	 * 对应<b>String</b>类型的名为<b>comment</b>的必要键值对<br>
	 * <b>private byte[]</b> _image;<br>
	 * 对应<b>Data</b>类型的名为<b>image</b>的可选键值对
	 * 
	 * @author mura
	 */
	public static class BaseJSONBean {

	}

	/**
	 * 生成一个JSON格式Bean
	 * 
	 * @author mura
	 * @throws Exception
	 */
	private BaseJSONBean createJSONBean() throws Exception {
		Class<?>[] classes = this.getClass().getClasses();
		for (Class<?> clazz : classes) {
			if (BaseJSONBean.class.isAssignableFrom(clazz)) {
				BaseJSONBean bean = (BaseJSONBean) clazz.newInstance();
				return bean;
			}
		}
		return null;
	}

	/**
	 * 使用JSON格式Bean为JSON对象赋值
	 * 
	 * @param bean
	 *            JSON格式Bean
	 * @param json
	 *            JSON对象
	 * @throws Exception
	 */
	private void assignJSONObject(J bean, JSONObject json) throws Exception {
		// 获取成员变量数组并遍历
		Field[] fields = bean.getClass().getFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			Class<?> fieldType = field.getType();
			if (List.class.isAssignableFrom(fieldType)) {
				// 数组类型
				@SuppressWarnings("unchecked")
				List<Object> fieldValue = (List<Object>) field.get(bean);
				JSONArray array = new JSONArray(fieldName);
				for (Object item : fieldValue) {
					Class<?> itemType = item.getClass();
					array.put(createJSONVariable(null, item, itemType));
				}
				json.put(array);
			} else {
				// 单个值类型
				Object fieldValue = field.get(bean);
				if (fieldName.charAt(0) == '_') {
					// 可选键值对
					fieldName = fieldName.substring(1);
					if (fieldValue == null) {
						continue;
					}
				}
				// 赋值
				json.put(createJSONVariable(fieldName, fieldValue, fieldType));
			}

		}
	}

	/**
	 * 使用JSON格式Bean的成员变量生成相应的JSONVariable对象
	 * 
	 * @param key
	 *            键名
	 * @param value
	 *            值
	 * @param type
	 *            值类型
	 * @return 生成的JSONVariable对象
	 * @throws InvalidBeanFieldTypeException
	 *             由于Bean的类型不正确导致的异常
	 */
	private JSONVariable createJSONVariable(String key, Object value, Class<?> type)
			throws InvalidBeanFieldTypeException {
		if (type.equals(Integer.class)) {
			return new JSONInteger(key, (Integer) value);
		} else if (type.equals(Float.class)) {
			return new JSONFloat(key, (Float) value);
		} else if (type.equals(Boolean.class)) {
			return new JSONBoolean(key, (Boolean) value);
		} else if (type.equals(String.class)) {
			return new JSONString(key, (String) value);
		} else if (type.equals(byte[].class)) {
			return new JSONData(key, (byte[]) value);
		} else {
			throw new InvalidBeanFieldTypeException(
					"Invalid JSON field: " + key + " type: " + TYPE_DESCRIPTION.get(type));
		}
	}

	/**
	 * 请求参数Bean或JSON格式Bean成员变量错误而引发的异常
	 * 
	 * @author mura
	 */
	public static class InvalidBeanFieldTypeException extends Exception {

		private static final long serialVersionUID = 1L;

		public InvalidBeanFieldTypeException(String msg) {
			super(msg);
		}
	}
}
