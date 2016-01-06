package edu.sysu.ncps.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mura.json.JSONArray;
import org.mura.json.JSONObject;
import org.mura.json.JSONVariable;

public abstract class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String JKEY_BOOLEAN_SUCCESS = "success";
	private static final String JKEY_STRING_ERROR_TYPE = "errorType";
	private static final String JKEY_STRING_ERROR_MESSAGE = "errorMsg";

	/**
	 * 处理请求的主方法
	 * 
	 * @param wrapper
	 *            参数打包对象, 需要转换为子类自行实现的类型
	 * @param json
	 *            输出用的JSON对象
	 * @return 错误信息, 无错误返回<b>null</b>
	 * @throws Exception
	 *             可能抛出的异常
	 */
	abstract protected String main(ParaWrapper wrapper, JSONObject json) throws Exception;

	/**
	 * doGet, 不直接修改此方法
	 */
	final protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGetAndPost(request, response);
	}

	/**
	 * doPost, 不直接修改此方法
	 */
	final protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGetAndPost(request, response);
	}

	/**
	 * 处理GET请求和POST请求
	 * 
	 * @param request
	 *            请求对象
	 * @param response
	 *            应答对象
	 * @throws ServletException
	 *             抛出的Servlet异常
	 * @throws IOException
	 *             抛出的IO异常
	 */
	private void doGetAndPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 设置编码格式
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		// response.setContentType("application/json");
		response.setContentType("text/plain");

		// 创建用于返回的JSON对象
		JSONObject json = new JSONObject(null);
		// 将传入参数打包为JSON对象
		JSONObject paraJson = parseParameters(request);

		// 检查传入参数是否被正确解析
		if (paraJson == null) {
			connectFail(json, "Cannot parse request parameters", "null");
		} else {
			try {
				String error = null;

				// 创建参数打包的对象
				ParaWrapper wrapper = createParaWrapper();
				// 对传入参数进行检查并赋值

				error = checkAndAssign(paraJson, wrapper);
				if (error != null) {
					connectFail(json, "Illegal parameters", error);
				} else {
					// 提供该Servlet需要提供的服务
					error = main(wrapper, json);
					if (error == null) {
						connectSuccess(json);
					} else {
						connectFail(json, "Failed to handle request in main()", error);
					}
				}
			} catch (Exception e) {
				// 发现了未捕获的异常
				connectFail(json, e.toString(), e.getMessage());
				e.printStackTrace();
			}
		}

		// 返回JSON对象生成的字符串
		PrintWriter out = response.getWriter();
		out.print(json.toFormatString());
	}

	/**
	 * 根据传入参数生成JSON对象, 方便解析
	 * 
	 * @param request
	 *            请求对象
	 * @return 解析成功返回参数JSON对象, 出现错误返回<b>null</b>
	 */
	private JSONObject parseParameters(HttpServletRequest request) {
		// 获取参数Map和打包JSON对象
		Map<String, String[]> paraMap = request.getParameterMap();
		JSONObject para = new JSONObject(null);

		// 便利所有的参数
		for (String key : paraMap.keySet()) {
			String[] array = paraMap.get(key);
			// 检查键名长度, 至少应大于1(前缀占1位)
			if (key.length() > 1) {
				char prefix = key.charAt(0);
				StringBuilder sb = new StringBuilder();
				sb.append(Character.toLowerCase(key.charAt(1)));
				if (key.length() > 2) {
					sb.append(key.substring(2));
				}
				String keyName = sb.toString();
				// try&catch字符串解析错误
				try {
					if (!Character.isUpperCase(prefix)) {
						// 是小写前缀, 说明不是数组类型, 检查是否有多个值
						if (array.length > 1) {
							return null;
						}
						String value = array[0];
						switch (prefix) {
						case 'i':
							para.put(keyName, Integer.parseInt(value));
							break;
						case 'f':
							para.put(keyName, Float.parseFloat(value));
							break;
						case 'b':
							para.put(keyName, Boolean.parseBoolean(value));
							break;
						case 's':
							para.put(keyName, value);
							break;
						case 'd':
							para.put(keyName, Base64.getDecoder().decode(value));
							break;
						default:
							return null;
						}
					} else {
						// 是数组类型, 遍历其所有内容
						para.putArray(keyName);
						JSONArray jArray = (JSONArray) para.get(keyName);
						for (String value : array) {
							switch (prefix) {
							case 'I':
								jArray.put(Integer.parseInt(value));
								break;
							case 'F':
								jArray.put(Float.parseFloat(value));
								break;
							case 'B':
								jArray.put(Boolean.parseBoolean(value));
								break;
							case 'S':
								jArray.put(value);
								break;
							case 'D':
								jArray.put(Base64.getDecoder().decode(value));
								break;
							default:
								return null;
							}
						}
					}
				} catch (Exception e) {
					return null;
				}
			} else {
				return null;
			}
		}
		return para;
	}

	/**
	 * 检查参数的合法性, 并在允许的情况下为其赋值
	 * 
	 * @return 成功返回<b>null</b>, 非法返回错误信息(String类型)
	 * @throws Exception
	 *             可能抛出的异常
	 */
	private String checkAndAssign(JSONObject paraJson, ParaWrapper wrapper) throws Exception {

		// 配置必要参数和可选参数的前缀
		final char PREFIX_REQUIRED = 'r';
		final char PREFIX_OPTIONAL = 'o';

		// 用于更清晰地在错误信息中类型名, 设置一组类型名和Java类型的映射
		final Map<Class<?>, String> TYPE_DESCRIPTION = new HashMap<Class<?>, String>() {
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

		// 获取成员变量数组并遍历
		Field[] fields = wrapper.getClass().getDeclaredFields();
		for (Field field : fields) {
			// 寻找以特殊字符开头的变量
			StringBuilder sb = new StringBuilder();
			String fieldName = field.getName();
			if (fieldName.length() > 1) {
				sb.append(Character.toLowerCase(fieldName.charAt(1)));
				if (fieldName.length() > 2) {
					sb.append(fieldName.substring(2));
				}
			}
			String varName = sb.toString();
			if (matchPrefix(field.getName(), PREFIX_REQUIRED)) {
				// 必要参数
				if (!paraJson.contains(varName)) {
					// 必要参数不存在
					return "Missing required parameter: " + varName + ", type: "
							+ TYPE_DESCRIPTION.get(field.getType());
				}
				// 存在此参数, 检查类型及赋值
				JSONVariable para = paraJson.get(varName);
				if (para.getValueClass().equals(field.getType())) {
					// 类型正确, 进行赋值
					field.set(wrapper, para.getValue());
				} else {
					// 类型错误
					return "Type error with required parameter: " + varName + ", current: "
							+ TYPE_DESCRIPTION.get(para.getValueClass()) + ", required: "
							+ TYPE_DESCRIPTION.get(field.getType());
				}
			} else if (matchPrefix(field.getName(), PREFIX_OPTIONAL)) {
				// 可选参数
				if (paraJson.contains(varName)) {
					// 存在此参数, 继续检查类型
					JSONVariable para = paraJson.get(varName);
					if (para.getValueClass().equals(field.getType())) {
						field.set(wrapper, para.getValue());
					}
				}
			}
		}
		return null;
	}

	/**
	 * 检查一个驼峰式命名的字符串是否符合某个前缀
	 * 
	 * @param str
	 *            字符串
	 * @param prefix
	 *            前缀
	 * @return <b>true</b>/<b>false</b>
	 */
	private boolean matchPrefix(String str, char prefix) {
		if (str.length() > 1) {
			if (str.charAt(0) == prefix) {
				if (Character.isUpperCase(str.charAt(1))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 请求失败, 返回请求失败信息
	 * 
	 * @param json
	 *            返回信息的JSON对象
	 * @param errorType
	 *            错误类型
	 * @param errorMessage
	 *            错误信息
	 */
	private void connectFail(JSONObject json, String errorType, String errorMessage) {
		json.clear();
		json.put(JKEY_BOOLEAN_SUCCESS, false);
		json.put(JKEY_STRING_ERROR_TYPE, errorType);
		json.put(JKEY_STRING_ERROR_MESSAGE, errorMessage);
	}

	/**
	 * 请求成功, 返回请求成功信息
	 * 
	 * @param json
	 */
	private void connectSuccess(JSONObject json) {
		json.put(JKEY_BOOLEAN_SUCCESS, true);
	}

	/**
	 * 打印信息至控制台<br>
	 * 如果需要打印至<b>syso</b>以外的地方, 修改此方法即可
	 * 
	 * @param msg
	 *            打印的内容
	 */
	protected void print(String msg) {
		System.out.println(msg);
	}

	/**
	 * 用于保存请求参数的Parameter类, 继承使用
	 * <p>
	 * 在子类中, 需要定义好从客户端发来的参数及其类型, 变量格式如下:<br>
	 * 变量名: <b>r</b>/<b>o</b> + (键名, 首字母大写)<br>
	 * <b>"r"</b>代表必须提供的参数, 若此参数不存在, 将会报错并不能执行请求<br>
	 * <b>"o"</b>代表可选参数, 此参数若存在则会赋值, 不然会为空, 类型错误时不会赋值<br>
	 * 在请求中出现了而在<b>Servlet</b>没有处理的参数, 不会影响执行
	 * <p>
	 * 类型关系表: (JSON -> Java)<br>
	 * Integer -> <b>Integer</b><br>
	 * Float -> <b>Float</b><br>
	 * Bool -> <b>Boolean</b><br>
	 * String -> <b>String</b><br>
	 * Data -> <b>byte[]</b><br>
	 * &lt;Any&gt;Array -> <b>List&lt;Object&gt;</b><br>
	 * <p>
	 * 变量命名举例:<br>
	 * <b>private String</b> rComment;<br>
	 * 对应<b>String</b>类型的名为<b>comment</b>的必要参数<br>
	 * <b>private byte[]</b> oImage;<br>
	 * 对应<b>Data</b>类型的名为<b>image</b>的可选参数
	 * 
	 * @author mura
	 */
	protected abstract class ParaWrapper {
	}

	/**
	 * 子类根据自身的实现, 生成相应的ParaWrapper对象
	 * 
	 * @return 新生成的ParaWrapper对象
	 */
	protected abstract ParaWrapper createParaWrapper();

}
