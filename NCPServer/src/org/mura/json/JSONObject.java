package org.mura.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JSONObject: JSON对象类
 * <p>
 * 对应JSON中的对象类型, 是一个键值对的集合<br>
 * 可以在其中添加所有类型的JSON变量, 每个变量必须有不重复的键名
 * 
 * @author mura
 */
@SuppressWarnings("rawtypes")
public class JSONObject extends JSONVariable<Map<String, JSONVariable>> implements JSONCollection {

	public JSONObject(String key) {
		super(key, new HashMap<String, JSONVariable>());
	}

	/**
	 * 添加一个JSON变量
	 * <p>
	 * 添加进JSONObject中的各JSON变量不依照排序获取, 应通过<b>键名</b>获取<br>
	 * 在添加进JSONObject后继续对JSON变量对象进行修改, <b>会导致JSONObject中的值也发生改变(储存的是引用)</b>
	 * <p>
	 * 任何种类的JSON变量都可以添加, 包括其他JSON对象和JSON数组<br>
	 * 添加的任何JSON变量都<b>必须有键名</b>, 不然不能被添加
	 * <p>
	 * 如果已经存在相同的键名, 会覆盖其原有值, 不然会创建新的键值对并赋值<br>
	 * 为数组添加内容, 需要在获取数组对象猴向内添加内容, 不提供包装方法
	 * 
	 * @param var
	 *            要添加到对象中的JSON变量
	 */
	public void add(JSONVariable var) {
		String key = var.key;
		// 必须存在键名, 才可以添加(Object中不允许存在匿名的成员)
		if (key != null) {
			if (value.containsKey(key)) {
				value.remove(key);
			}
			value.put(key, var);
		}
	}

	/**
	 * 获取一个JSON变量
	 * 
	 * @param key
	 *            键名
	 * @return 此键名有效时返回对应的JSON变量<br>
	 *         键名无效时返回<b>null</b>
	 */
	public JSONVariable get(String key) {
		return value.get(key);
	}

	/**
	 * 删除一个JSON变量
	 * 
	 * @param key
	 *            键名
	 * @return 成功删除时返回<b>true</b><br>
	 *         键名无效无法删除时返回<b>false</b>
	 */
	public boolean remove(String key) {
		if (value.remove(key) != null) {
			return true;
		}
		return false;
	}

	/**
	 * 获取对象所有元素的键名的Set对象
	 * 
	 * @return Set对象
	 */
	public Set<String> keySet() {
		return value.keySet();
	}

	/**
	 * 检查是否存在某个键名
	 * 
	 * @param key
	 *            键名
	 * @return 存在时返回<b>true</b>, 否则返回<b>false</b>
	 */
	public boolean contains(String key) {
		return value.containsKey(key);
	}

	/**
	 * 清空此JSON对象
	 */
	public void clear() {
		value.clear();
	}

	/**
	 * 获取此JSON对象中元素的个数
	 * 
	 * @return 元素的个数
	 */
	public int size() {
		return value.size();
	}

	/**
	 * 添加一个空的数组
	 * 
	 * @param key
	 *            键名
	 */
	public void addArray(String key) {
		add(new JSONArray(key));
	}

	/**
	 * 添加一个整型JSON变量
	 * 
	 * @param key
	 *            键名
	 * @param value
	 *            值
	 */
	public void add(String key, Integer value) {
		add(new JSONInteger(key, value));
	}

	/**
	 * 添加一个浮点型JSON变量
	 * 
	 * @param key
	 *            键名
	 * @param value
	 *            值
	 */
	public void add(String key, Float value) {
		add(new JSONFloat(key, value));
	}

	/**
	 * 添加一个布尔型JSON变量
	 * 
	 * @param key
	 *            键名
	 * @param value
	 *            值
	 */
	public void add(String key, Boolean value) {
		add(new JSONBoolean(key, value));
	}

	/**
	 * 添加一个字符串型JSON变量
	 * 
	 * @param key
	 *            键名
	 * @param value
	 *            值
	 */
	public void add(String key, String value) {
		add(new JSONString(key, value));
	}

	/**
	 * 添加一个二进制型JSON变量
	 * 
	 * @param key
	 *            键名
	 * @param value
	 *            值
	 */
	public void add(String key, byte[] value) {
		add(new JSONData(key, value));
	}

	@Override
	protected String valueString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (String key : value.keySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(value.get(key).toString());
		}
		sb.append("}");
		return sb.toString();
	}
}
