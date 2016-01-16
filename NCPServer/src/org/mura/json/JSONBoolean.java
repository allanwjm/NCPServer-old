package org.mura.json;

/**
 * JSONBoolean: JSON布尔型类
 * <p>
 * 对应JSON中的布尔类型, 可以存储一个true/false值<br>
 * 
 * @author mura
 */
public class JSONBoolean extends JSONVariable<Boolean> {

	public JSONBoolean(String key) {
		super(key);
	}
	
	public JSONBoolean(String key, Boolean value) {
		super(key, value);
	}

	@Override
	protected String valueString() {
		return value + "";
	}
}
