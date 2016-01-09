package edu.sysu.ncps.servlet.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

/**
 * Servlet基类, 继承此类后开发实现具体功能的Servlet
 * <p>
 * 实现一个新的子类需要做的工作:
 * <p>
 * 1、需要设置两个Bean(输入与输出)的类型:<br>
 * <b>P</b>为请求参数Bean的类型, 请求参数类应继承自<b>BaseServlet.BaseParaBean</b><br>
 * <b>J</b>为JSON格式Bean的类型, 请求参数类应继承自<b>BaseServlet.BaseJSONBean</b><br>
 * 子类具体的格式请参照两个父类的Javadoc说明
 * <p>
 * 2、需要实现主方法:<br>
 * 在<b>main</b>方法中处理请求<br>
 * 请求参数可以在传入的<b>para</b>对象中获得, 然后将返回内容写在<b>json</b>对象中<br>
 * 如果出现异常可以直接<b>throw</b>, 将会显示错误信息, 方便调试
 * 
 * @author mura
 *
 * @param
 * 			<P>
 *            ParaBean类型
 * @param <J>
 *            JSONBean类型
 */
public abstract class BaseServlet<P extends BaseParaBean, J extends BaseJSONBean> extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * 是否使用浏览器调试标识位(影响返回的格式)
	 */
	protected static final boolean BROWSER_DEBUG = true;

	// 用于更清晰地在错误信息中类型名, 设置一组类型名和Java类型的映射
	protected static final Map<Class<?>, String> TYPE_DESCRIPTION = new HashMap<Class<?>, String>() {

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
	 * @param para
	 *            请求参数Bean, 需继承自BaseParaBean
	 * @param json
	 *            JSON格式Bean, 需继承自BaseJSONBean
	 * @param servlet
	 *            提供未包装的对象访问, 除非必要, 不要用
	 * @throws Exception
	 *             如果处理过程出现异常, 抛出异常中断请求处理
	 */
	abstract protected void main(P para, J json, Servlet servlet) throws Exception;

	/**
	 * doGet, 不直接使用此方法
	 */
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handleRequest(request, response);
	}

	/**
	 * doPost, 不直接使用此方法
	 */
	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handleRequest(request, response);
	}

	/**
	 * 处理GET请求和POST请求
	 * 
	 * @param request
	 *            请求对象
	 * @param response
	 *            应答对象
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
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

		// 创建输出JSON对象
		JSONObject jsonObj = new JSONObject(null);

		// 创建Servlet包装对象
		Servlet servlet = new Servlet(request, response, out);

		try {
			// 创建参数打包的对象
			P para = (P) createParaBean();
			J json = (J) createJSONBean();
			assignParaBean(request, para);

			// 调用主方法处理请求
			main(para, json, servlet);
			assignJSONObject(json, jsonObj);

			if (servlet.print) {
				// 打印返回输出JSON对象
				if (BROWSER_DEBUG) {
					out.print(jsonObj.toFormatString());
				} else {
					out.print(jsonObj.toString());
				}
			}

		} catch (Exception e) {
			// 捕获并处理异常, 返回相应信息
			servlet.catching = true;
			try {
				handleException(servlet, e);
			} catch (Exception e2) {
				// 如果仍然抛出错误, 改用默认方式处理异常
				defaultHandleException(servlet, e2);
			}
		}
	}

	/**
	 * 处理异常, 并返回错误信息<br>
	 * 默认的处理方法是打印至控制台和返回页面
	 * 
	 * @param servlet
	 * @param e
	 * @throws Exception
	 *             允许再次抛出异常
	 */
	protected void handleException(Servlet servlet, Exception e) throws Exception {
		defaultHandleException(servlet, e);
	}

	/**
	 * 处理默认的默认方式, 不会再次抛出异常
	 * 
	 * @param servlet
	 * @param e
	 */
	private final void defaultHandleException(Servlet servlet, Exception e) {
		e.printStackTrace();
		servlet.out.print(e.getMessage());
	}

	/**
	 * 生成一个请求参数Bean
	 * 
	 * @throws Exception
	 * @author mura
	 */
	private BaseParaBean createParaBean() throws Exception {
		Class<?> clazz = getGenericType(0);
		return (BaseParaBean) clazz.newInstance();
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
	private void assignParaBean(HttpServletRequest request, BaseParaBean bean) throws Exception {

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
					throw new BeanFieldException("Missing required parameter: " + fieldName + ", type: "
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
					// 作为字符串形式存储, 如果需要转换, 通过其它静态方法再转换
					list.add(value);
				}
				field.set(bean, list);
			} else {
				// 此值不为数组, 直接赋第一个值
				String value = paraMap.get(fieldName)[0];
				field.set(bean, parseParaValue(fieldName, value, fieldType));
			}
		}
	}

	/**
	 * 将字符串形式的请求参数分析为特定的类型
	 * 
	 * @param key
	 *            该参数的键名
	 * @param value
	 *            字符串参数
	 * @param type
	 *            类型
	 * @return 分析后的Object
	 * @throws BeanFieldException
	 *             由于Bean的类型不正确导致的异常
	 */
	private Object parseParaValue(String key, String value, Class<?> type) throws BeanFieldException {
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
				throw new BeanFieldException("Invalid ParaBean field: " + key + " type: " + TYPE_DESCRIPTION.get(type));
			}
		} catch (IllegalArgumentException e) {
			throw new BeanFieldException(
					"Cannot parse field: " + key + " ,value: " + value + ", into type: " + TYPE_DESCRIPTION.get(type));
		}
	}

	/**
	 * 生成一个JSON格式Bean
	 * 
	 * @author mura
	 * @throws Exception
	 */
	private BaseJSONBean createJSONBean() throws Exception {
		Class<?> clazz = getGenericType(1);
		return (BaseJSONBean) clazz.newInstance();
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
	private void assignJSONObject(BaseJSONBean bean, JSONObject json) throws Exception {
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
					array.add(createJSONVariable(null, item, itemType));
				}
				json.add(array);
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
				json.add(createJSONVariable(fieldName, fieldValue, fieldType));
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
	 * @throws BeanFieldException
	 *             由于Bean的类型不正确导致的异常
	 */
	@SuppressWarnings("rawtypes")
	private JSONVariable createJSONVariable(String key, Object value, Class<?> type) throws BeanFieldException {
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
			throw new BeanFieldException("Invalid JSONBean field: " + key + ", type: " + type.getName());
		}
	}

	private Class<?> getGenericType(int index) {

		Type genType = getClass().getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			throw new RuntimeException("Index outof bounds");
		}

		if (!(params[index] instanceof Class)) {
			return Object.class;
		}

		return (Class<?>) params[index];
	}

	/**
	 * 将Servlet中的各种对象包装类, 提供未包装的对象访问, 允许实现自定义的复杂功能
	 * 
	 * @author mura
	 */
	public class Servlet {

		/**
		 * 请求对象
		 */
		public final HttpServletRequest request;

		/**
		 * 应答对象
		 */
		public final HttpServletResponse response;

		/**
		 * 输出流对象
		 */
		public final PrintWriter out;

		/**
		 * 是否正在处理异常标识位
		 */
		public boolean catching;

		/**
		 * 是否打印输出JSON标识位
		 */
		public boolean print;

		/**
		 * 构造方法
		 */
		public Servlet(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
			this.request = request;
			this.response = response;
			this.out = out;
			this.catching = false;
			this.print = true;
		}

		/**
		 * (!只可在异常处理中调用)<br>
		 * 打印JSON格式Bean, 在出现了异常的情况下仍然进行返回<br>
		 * 此类型与泛型类型可以不同, 在<b>main</b>中已经进行的赋值也会无效(重新创建的JSON对象)
		 * 
		 * @param json
		 * @throws Exception
		 */
		public void forcePrintJSON(BaseJSONBean json) throws Exception {
			if (this.catching) {
				// 创建一个新的JSONObject对象
				JSONObject jsonObj = new JSONObject(null);
				assignJSONObject(json, jsonObj);
				// 打印返回输出JSON对象
				if (BROWSER_DEBUG) {
					out.print(jsonObj.toFormatString());
				} else {
					out.print(jsonObj.toString());
				}
			}
		}

		/**
		 * (!只可在<b>main</b>中调用)<br>
		 * 在服务结束时, 跳过打印JSON字符串
		 */
		public void skipPrintJSON() {
			this.print = false;
		}
	}
}
