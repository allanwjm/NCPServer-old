package org.mura.json;

/**
 * JSONFloat: JSON浮点型类
 * <p>
 * 对应JSON中的浮点类型, 可以存储一个带有小数点的数值<br>
 * 
 * @author mura
 */
public class JSONFloat extends JSONVariable<Float> {

	public JSONFloat(String key) {
		super(key);
	}

	public JSONFloat(String key, Float value) {
		super(key, value);
	}

	@Override
	protected String valueString() {
		return value + "";
	}
}
