package edu.sysu.ncps.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mura.json.JSONArray;
import org.mura.json.JSONObject;

public abstract class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String JKEY_BOOLEAN_SUCCESS = "success";
	private static final String JKEY_STRING_ERROR_TYPE = "errorType";
	private static final String JKEY_STRING_ERROR_MESSAGE = "errorMsg";

	/**
	 * 检查参数合法性
	 * 
	 * @param para
	 *            参数对象
	 * @return 合法返回<b>null</b>, 非法返回错误信息(String类型)
	 * @throws Exception
	 *             可能抛出的异常
	 */
	abstract protected String check(JSONObject para) throws Exception;

	/**
	 * 处理请求的主方法
	 * 
	 * @param para
	 *            传入的参数, 打包为JSON对象
	 * @param json
	 *            组织传出参数的JSON对象
	 * @return 正常处理返回<b>null</b>, 出现异常返回错误信息(String类型)
	 * @throws Exception
	 *             可能抛出的异常
	 */
	abstract protected String main(JSONObject para, JSONObject json) throws Exception;

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
		response.setContentType("application/json");

		// 创建用于返回的JSON对象
		JSONObject json = new JSONObject(null);
		// 将传入参数打包为JSON对象
		JSONObject para = parseParameters(request);

		// 检查传入参数是否被正确解析
		if (para == null) {
			connectFail(json, "Cannot parse request parameters!", "null");
		} else {
			try {
				String error = null;
				// 对传入参数进行检查
				error = check(para);
				if (error != null) {
					connectFail(json, "Failure in check().", error);
				} else {
					// 提供该Servlet需要提供的服务
					error = main(para, json);
					if (error == null) {
						connectSuccess(json);
					} else {
						connectFail(json, "Failure in main().", error);
					}
				}
			} catch (Exception e) {
				// 发现了未捕获的异常
				connectFail(json, e.toString(), e.getMessage());
				print(e.getMessage());
			}
		}

		// 返回JSON对象生成的字符串
		PrintWriter out = response.getWriter();
		out.print(json.toString());
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
			if (key.length() > 1) {
				char prefix = key.charAt(0);
				String keyName = key.substring(1, key.length() - 1);
				// try/catch字符串解析错误
				try {
					if (!Character.isUpperCase(prefix)) {
						// 是小写前缀, 说明不是数组类型
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

}
