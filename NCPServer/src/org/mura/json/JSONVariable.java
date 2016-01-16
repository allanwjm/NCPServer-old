package org.mura.json;

/**
 * JSONVariable: JSON变量类(抽象)
 * <p>
 * 各种JSON类型的基类, 继承后使用
 * 
 * @author mura
 */
public abstract class JSONVariable<T> {

	/**
	 * JSON变量的键名(final)<br>
	 * 在初始化时设定, 设定好后不可更改
	 */
	protected final String key;

	/**
	 * 值, 根据不同的实现, 替换为不同的类型
	 */
	protected T value;

	/**
	 * 构造方法
	 * <p>
	 * 声明了带参数的构造方法, 不可以通过无参构造方法创建对象<br>
	 * 如果需要没有键名的JSON变量(如用于数组中或者根对象), 请传入<b>null</b><br>
	 * <b>值为null</b>
	 * 
	 * @param key
	 *            键名
	 */
	public JSONVariable(String key) {
		this.key = key;
		this.value = null;
	}

	/**
	 * 带有初始值的构造方法
	 * 
	 * @param key
	 *            键名(可以为<b>null</b>
	 * @param value
	 *            初始值
	 */
	public JSONVariable(String key, T value) {
		this.key = key;
		setValue(value);
	}

	/**
	 * toString方法(重载)
	 * <p>
	 * 通过这个方法将JSON变量转换为对应的字符串<br>
	 * 会按照如下格式输出: "(键名)":(值)<br>
	 * 如果只需要值部分, 请使用toStringValue()方法
	 */
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		if (key != null) {
			sb.append("\"" + key + "\":");
		}
		sb.append(valueString());
		return sb.toString();
	}

	/**
	 * 只输出值部分的转换字符串方法(抽象)
	 * <p>
	 * 必须重写此方法, 才可以正常转换为字符串<br>
	 * 需要在此方法中返回组织好的表示JSON变量值(或对象成员, 数组内容等)的字符串
	 * 
	 * @return
	 */
	protected abstract String valueString();

	/**
	 * 获取值的类型
	 * 
	 * @return 值类型
	 */
	public Class<?> getValueClass() {
		return value.getClass();
	}

	/**
	 * 获取值内容
	 * 
	 * @return 值内容
	 */
	public T getValue() {
		return value;
	}

	/**
	 * 设置值内容
	 * 
	 * @param value
	 *            值内容
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * 输出格式化后的String, 方便输出和查看
	 * 
	 * @return 格式化后的String
	 */
	public String toFormatString() {
		String str = toString();
		StringBuilder buff = new StringBuilder();
		int indent = 0;
		int quotation = 0;
		int newLineStart = 2;
		int bracketStart = 0;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '\"') {
				quotation++;
			}
			newLineStart--;
			bracketStart--;
			if (quotation % 2 == 0) {
				switch (ch) {
				case '{':
				case '[':
					if (newLineStart <= 0) {
						buff.append(newLine(indent));
						newLineStart = 2;
					}
					indent++;
					buff.append(ch);
					buff.append(newLine(indent));
					newLineStart = 2;
					bracketStart = 2;
					break;
				case '}':
				case ']':
					indent--;
					if (bracketStart > 0) {
						while (buff.charAt(buff.length() - 1) != '{' && buff.charAt(buff.length() - 1) != '[') {
							buff.deleteCharAt(buff.length() - 1);
						}
						buff.append(ch);
					} else {
						buff.append(newLine(indent));
						newLineStart = 2;
						buff.append(ch);
					}
					break;
				case ',':
					buff.append(ch);
					buff.append(newLine(indent));
					newLineStart = 2;
					break;
				case ':':
					buff.append(ch);
					buff.append(' ');
					break;
				default:
					buff.append(ch);
					break;
				}
			} else {
				buff.append(ch);
				if (ch == '\n') {
					for (int j = 0; j < indent; j++) {
						buff.append("    ");
					}
				}
			}
		}
		return buff.toString();
	}

	/**
	 * 根据指定的缩进值, 插入新行
	 * 
	 * @param indent
	 *            缩进值
	 * @return 可以附加至StringBuilder的字符串
	 */
	private String newLine(int indent) {
		StringBuilder buff = new StringBuilder();
		buff.append('\n');
		for (int i = 0; i < indent; i++) {
			buff.append("    ");
		}
		return buff.toString();
	}

}
