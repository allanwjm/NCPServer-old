package org.mura.json;

import java.util.Base64;

/**
 * JSONData: JSON二进制类
 * <p>
 * 对应二进制类型, 在JSON中通过Base64编码传输<br>
 * 
 * @author mura
 */
public class JSONData extends JSONVariable<byte[]> {

	public JSONData(String key) {
		super(key);
	}

	public JSONData(String key, byte[] value) {
		super(key, value);
	}

	public JSONData(String key, String value) {
		super(key, Base64.getDecoder().decode(value));
	}

	@Override
	protected String valueString() {
		if (value == null) {
			// null, 不进行编码直接输出null
			return "null";
		}
		// 编码后再输出
		return "\"" + Base64.getEncoder().encodeToString(value) + "\"";
	}
}
