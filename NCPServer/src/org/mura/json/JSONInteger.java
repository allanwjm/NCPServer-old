package org.mura.json;

/**
 * JSONInteger: JSON整型类
 * <p>
 * 对应JSON中的整数类型, 可以存储一个整数值<br>
 * 
 * @author mura
 */
public class JSONInteger extends JSONVariable<Integer> {

	public JSONInteger(String key) {
		super(key);
	}

	public JSONInteger(String key, Integer value) {
		super(key, value);
	}

	@Override
	protected String valueString() {
		return value + "";
	}
}
