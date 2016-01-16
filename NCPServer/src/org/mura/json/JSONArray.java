package org.mura.json;

import java.util.ArrayList;
import java.util.List;

/**
 * JSONArray: JSON数组类
 * <p>
 * 对应JSON中的数组类型, 可以储存多个JSON变量, 无类型限制<br>
 * 也可以保存多个JSON对象或是其他JSON数组
 * <p>
 * 当输出数组时, 数组中元素的键名将被忽略
 * 
 * @author mura
 */
@SuppressWarnings("rawtypes")
public class JSONArray extends JSONVariable<List<JSONVariable>> implements JSONCollection {

	public JSONArray(String key) {
		super(key, new ArrayList<JSONVariable>());
	}

	/**
	 * 添加一个JSON变量
	 * <p>
	 * 添加进JSONArray中的各JSON变量是有序的, 可以通过索引获取<br>
	 * 在添加进JSONArray后继续对JSON变量对象进行修改, 会导致JSONArray中的值也发生改变(储存的是引用)
	 * <p>
	 * 任何种类的JSON变量都可以添加, 包括其他JSON对象和JSON数组<br>
	 * 添加后不会改变键名, 但是在输出数组字符串时, 不会输出键名
	 * 
	 * @param var
	 *            要添加到数组中的JSON变量
	 */
	public void add(JSONVariable var) {
		value.add(var);
	}

	/**
	 * 获取一个JSON变量
	 * 
	 * @param index
	 *            索引
	 * @return 此索引有效时返回对应的JSON变量<br>
	 *         索引无效时返回<b>null</b>
	 */
	public JSONVariable get(int index) {
		if (index < value.size()) {
			return value.get(index);
		}
		return null;
	}

	/**
	 * 删除一个JSON变量
	 * 
	 * @param index
	 *            索引
	 * @return 成功删除时返回<b>true</b><br>
	 *         索引无效无法删除时返回<b>false</b>
	 */
	public boolean remove(int index) {
		if (index < value.size()) {
			value.remove(index);
			return true;
		}
		return false;
	}

	/**
	 * 获取数组内容的List对象
	 * 
	 * @return List对象
	 */
	public List<JSONVariable> list() {
		return value;
	}

	/**
	 * 清空此JSON数组
	 */
	public void clear() {
		value.clear();
	}

	/**
	 * 获取此JSON数组中元素的个数
	 * 
	 * @return 元素的个数
	 */
	public int size() {
		return value.size();
	}

	/**
	 * 添加一个整型JSON变量
	 * 
	 * @param value
	 *            值
	 */
	public void add(Integer value) {
		add(new JSONInteger(null, value));
	}

	/**
	 * 添加一个浮点型JSON变量
	 * 
	 * @param value
	 *            值
	 */
	public void add(Float value) {
		add(new JSONFloat(null, value));
	}

	/**
	 * 添加一个布尔型JSON变量
	 * 
	 * @param value
	 *            值
	 */
	public void add(Boolean value) {
		add(new JSONBoolean(null, value));
	}

	/**
	 * 添加一个字符串型JSON变量
	 * 
	 * @param value
	 *            值
	 */
	public void add(String value) {
		add(new JSONString(null, value));
	}

	/**
	 * 添加一个二进制型JSON变量
	 * 
	 * @param value
	 *            值
	 */
	public void add(byte[] value) {
		add(new JSONData(null, value));
	}

	@Override
	protected String valueString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (JSONVariable item : value) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(item.valueString());
		}
		sb.append("]");
		return sb.toString();
	}
}
