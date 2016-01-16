package org.mura.json;

/**
 * JSONString: JSON字符串类
 * <p>
 * 对应JSON中的字符串类型, 可以存储一个字符串<br>
 * 
 * @author mura
 */
public class JSONString extends JSONVariable<String> {

	public JSONString(String key) {
		super(key);
	}

	public JSONString(String key, String value) {
		super(key, value);
	}

	@Override
	protected String valueString() {
		return "\"" + value + "\"";
	}
}
