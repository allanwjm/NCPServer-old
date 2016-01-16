package org.mura.json;

/**
 * JSONNull: JSON空类型
 * <p>
 * 用于保存JSON中的null值, 没有类型, 用于表示解析时的"null"<br>
 * 自己组织JSON对象时不要使用这个类型
 *
 * @author mura
 */
public class JSONNull extends JSONVariable<Object> {

	public JSONNull(String key) {
		super(key);
	}

	@Override
	public void setValue(Object value) {
		super.setValue(null);
	}

	@Override
	protected String valueString() {
		return "null";
	}
}
