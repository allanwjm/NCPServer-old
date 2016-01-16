package org.mura.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * JSON对象解析类
 * <p>
 * 用于将JSON代码解析为JSONObject对象
 * 
 * @author mura
 */
@SuppressWarnings("rawtypes")
public class JSONParser {

	/**
	 * 解析JSON代码
	 * 
	 * @param src
	 *            JSON代码
	 * @param key
	 *            生成JSON对象的键名
	 * @return 生成的JSON对象
	 * @throws JSONParseException
	 *             当JSON代码字符串解析错误时, 抛出此异常
	 */
	public static JSONObject parse(String src, String key) throws JSONParseException {
		JSONObject obj = new JSONObject(key);
		JSONLexicalAnalyzer lexical = new JSONLexicalAnalyzer(src);
		List<JSONWord> wList = lexical.lexicalAnalyse();
		JSONSyntaxAnalyzer syntax = new JSONSyntaxAnalyzer(wList);
		syntax.syntaxAnalyse(obj);
		return obj;
	}

	/**
	 * 报告一个JSON分析异常, 会终止分析进程
	 * 
	 * @param msg
	 *            异常信息
	 * @throws JSONParseException
	 *             异常对象
	 */
	private static void parseError(String msg) throws JSONParseException {
		throw new JSONParseException(msg);
	}

	/**
	 * JSONParseException: JSON代码分析异常
	 * <p>
	 * 用于报告在JSON解析中发生的各种异常
	 * 
	 * @author mura
	 *
	 */
	private static class JSONParseException extends Exception {

		private static final long serialVersionUID = 1L;

		/**
		 * 构造方法
		 * <p>
		 * 需要提供错误信息
		 * 
		 * @param msg
		 *            错误信息
		 */
		public JSONParseException(String msg) {
			super(msg);
		}
	}

	/**
	 * JSONWordTypeEnum: JSON单词类型枚举
	 * <p>
	 * 代表了各种不同的JSON单词类型, 便于分析和解析
	 * 
	 * @author mura
	 *
	 */
	private static enum JSONWordTypeEnum {
		UNDEFINED, COMMA, COLON, LBRACE, RBRACE, LBRACKET, RBRACKET, TRUE, FALSE, NULL, STRING, INTEGER, FLOAT, EXPONENT
	}

	/**
	 * JSONWord: JSON代码单词类
	 * <p>
	 * 是JSON词法分析器分析得来的结果, 含有代码原文和类型信息
	 * 
	 * @author mura
	 *
	 */
	private static class JSONWord {

		/**
		 * 代码原文
		 */
		private final String word;
		/**
		 * 单词类型, 是一个枚举值
		 */
		private final JSONWordTypeEnum type;

		/**
		 * 构造方法
		 * <p>
		 * JSON单词对象一旦赋值将无法修改!
		 * 
		 * @param word
		 *            由词法分析器分析得来的单个单词
		 * @param type
		 *            由词法分析器分析得来的单词类型
		 */
		public JSONWord(String word, JSONWordTypeEnum type) {
			this.word = word;
			this.type = type;
		}

		@Override
		public String toString() {
			return word;
		}

	}

	/**
	 * JSONLexicalAnalyzer: JSON代码词法分析器
	 * <p>
	 * 将JSON代码(String)分析为一系列的单词, 以便下一步分析
	 * 
	 * @author mura
	 */
	private static class JSONLexicalAnalyzer {

		/**
		 * JSON代码String缓存
		 */
		private String source;
		/**
		 * 指示当前分析进度的迭代器(索引下标)
		 */
		private int iterator;

		/**
		 * 构造方法
		 * <p>
		 * 需要提供一个含有JSON代码的String对象作为分析对象
		 * 
		 * @param src
		 *            JSON代码
		 */
		public JSONLexicalAnalyzer(String source) {
			this.source = source;
			iterator = 0;
		}

		/**
		 * 对JSON代码字符串进行词法分析, 返回单词列表
		 * <p>
		 * 返回的是JSONWord的List, 带有单词类型的信息
		 * 
		 * @return 单词列表对象
		 * @throws JSONParseException
		 *             当分析出词法错误时, 抛出此异常
		 */
		public List<JSONWord> lexicalAnalyse() throws JSONParseException {
			List<JSONWord> list = new ArrayList<>();
			String wordStr = null;
			while ((wordStr = nextWord()) != null) {
				JSONWordTypeEnum type = JSONWordTypeEnum.UNDEFINED;

				if (wordStr.equals(",")) {
					type = JSONWordTypeEnum.COMMA;
				} else if (wordStr.equals(":")) {
					type = JSONWordTypeEnum.COLON;
				} else if (wordStr.equals("{")) {
					type = JSONWordTypeEnum.LBRACE;
				} else if (wordStr.equals("}")) {
					type = JSONWordTypeEnum.RBRACE;
				} else if (wordStr.equals("[")) {
					type = JSONWordTypeEnum.LBRACKET;
				} else if (wordStr.equals("]")) {
					type = JSONWordTypeEnum.RBRACKET;
				} else if (wordStr.equals("true")) {
					type = JSONWordTypeEnum.TRUE;
				} else if (wordStr.equals("false")) {
					type = JSONWordTypeEnum.FALSE;
				} else if (wordStr.equals("null")) {
					type = JSONWordTypeEnum.NULL;
				} else if (wordStr.charAt(0) == '\"' && wordStr.charAt(wordStr.length() - 1) == '\"') {
					type = JSONWordTypeEnum.STRING;
					wordStr = wordStr.substring(1, wordStr.length() - 1);
				} else {
					// 只可能是数字, 检查是何种数字
					if (wordStr.indexOf('.') != -1) {
						// 含有小数点, 可能是浮点值
						try {
							Float.parseFloat(wordStr);
							type = JSONWordTypeEnum.FLOAT;
						} catch (NumberFormatException e) {
							// 解析失败, 不是这种类型
						}
					} else if (wordStr.indexOf('e') != -1 || wordStr.indexOf('E') != -1) {
						// 含有'e'或'E', 可能是科学计数法的浮点值
						try {
							Float.parseFloat(wordStr);
							type = JSONWordTypeEnum.EXPONENT;
						} catch (NumberFormatException e) {
							// 解析失败, 不是这种类型
						}
					} else {
						// 不含有'.', 'e'和'E', 可能是整数
						try {
							// 尝试解析其整数值
							Integer.parseInt(wordStr);
							type = JSONWordTypeEnum.INTEGER;
						} catch (NumberFormatException e) {
							// 解析失败, 不是这种类型
						}
					}
				}

				if (type == JSONWordTypeEnum.UNDEFINED) {
					// 未能分类至合法的类型中, 此单词非法
					parseError("Cannot catalog this word: (" + wordStr + ")");
				}

				JSONWord word = new JSONWord(wordStr, type);
				list.add(word);
			}
			return list;
		}

		/**
		 * 获取下一个单词
		 * <p>
		 * 将JSON代码逐个分析成单个分隔符或者标识符, 排除所有空白符<br>
		 * 读取完毕之后将返回<b>null</b>表示结束
		 * <p>
		 * 分隔符包含: "{", "}", "[", "]", ",", ":"等<br>
		 * 标识符包含: string, number, "true", "false", "null"等
		 * <p>
		 * 两个引号之间的部分作为一个整体, 会直接输出
		 * 
		 * @return 下一个单词或<b>null</b>
		 * @throws JSONParseException
		 *             当分析出词法错误时, 抛出此异常
		 */
		private String nextWord() throws JSONParseException {
			char ch = '\0';
			// 新建一个空白的缓冲区对象
			StringBuilder buff = new StringBuilder();
			boolean wait = true;
			boolean string = false;
			boolean escape = false;
			while (true) {
				if ((ch = nextChar()) == '\0') {
					// 已经读取到结尾
					if (buff.length() > 0) {
						// 缓冲区内有值(应当以单字符的"}"或其后的空白符结尾, 意味着格式错误)
						parseError("Unexpected ending of JSON source: \"" + buff.toString() + "\", should be \"}\".");
						return buff.toString();
					} else {
						// 没有值了, 返回null表示结束
						return null;
					}
				} else {
					// 还没有读取到结尾
					if (wait) {
						// 等待第一个非空白符的字符
						if (!Character.isWhitespace(ch)) {
							// 非空白符, 回滚一位将当前字符传入下次循环, 开始分析循环
							wait = false;
							rollback();
						}
					} else {
						// 分析和写入缓冲区, 这个循环将从第一个非空白符的字符开始
						if (!string) {
							// 如果不是正在处于字符串读取模式中(忽略所有内容直到下一个引号)
							if (ch == '\"') {
								// 如果是个字符串, 直到读取下一个双引号为止, 原样保存其中所有内容
								if (buff.length() != 0) {
									// 字符串应当以双引号开头, 而不是中间出现双引号
									parseError("String define error: Illegal quotation. Error appears near: ("
											+ buff.toString() + ").");
								} else {
									// 正常的字符串开头, 进入字符串模式
									string = true;
									buff.append(ch);
								}
							} else if (isSeparator(ch)) {
								// 如果当前字符是分隔符, 需要进行输出
								if (buff.length() > 0) {
									// 缓冲区长度大于1说明已经缓存了一个标识符, 需要将迭代器回滚一位,
									// 输出当前标识符, 下次输出当前的分隔符
									rollback();
									return buff.toString();
								} else {
									// 缓冲区长度等于一说明此次读取到了一个单独的标识符, 直接输出
									buff.append(ch);
									return buff.toString();
								}
							} else {
								// 是普通字符, 将其写入
								buff.append(ch);
							}
						} else {
							// 字符串模式
							if (!escape) {
								// 当前没有在读取转义字符
								if (ch == '\"') {
									// 读取到了结束的双引号, 结束字符串的读取
									buff.append(ch);
									return buff.toString();
								} else if (ch == '\\') {
									// 读取到了反斜杠, 开始分析转义字符(本次不写入)
									escape = true;
								} else if (!Character.isISOControl(ch)) {
									// 其他字符, 只要不是控制字符, 直接写入
									buff.append(ch);
								} else {
									// 出现了控制字符, 出错
									int hex = ch;
									parseError(String.format("iLLegal control character in  string, char code: (%x).",
											hex));
								}
							} else {
								// 转义字符模式, 读取一个转义字符并写入buff中
								escape = false;
								switch (ch) {
								case '\"':
									buff.append('\"');
									break;
								case '\\':
									buff.append('\\');
									break;
								case '/':
									buff.append('/');
									break;
								case 'b':
									buff.append('\b');
									break;
								case 'f':
									buff.append('\f');
									break;
								case 'n':
									buff.append('\n');
									break;
								case 'r':
									buff.append('\r');
									break;
								case 't':
									buff.append('\t');
									break;
								default:
									parseError("Illegal escape character in string: (\\" + ch + ").");
									break;
								}
							}
						}
					}
				}
			}
		}

		/**
		 * 获取下一个字符
		 * <p>
		 * 每次都会自动向后跳一个字符, 读取完毕后返回<b>'\0'</b>
		 * 
		 * @return 下一个字符或<b>'\0'</b>
		 */
		private char nextChar() {
			if (iterator < source.length()) {
				return source.charAt(iterator++);
			} else {
				return '\0';
			}
		}

		/**
		 * 回滚一个字符, 下次调用nextChar()时, 将继续读取同一个字符
		 */
		private void rollback() {
			if (iterator > 0) {
				iterator--;
			}
		}

		/**
		 * 检查特定字符是否属于某个字符集合
		 * 
		 * @param ch
		 *            要检查的字符
		 * @return <b>true/false</b>
		 */
		private static boolean isSeparator(char ch) {
			if (",:[]{} \t\r\n".indexOf(ch) != -1) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * JSON代码句法分析器
	 * <p>
	 * 解析词法分析其分析得到的单词列表, 构建单词之间的关系
	 * 
	 * @author mura
	 *
	 */
	private static class JSONSyntaxAnalyzer {

		/**
		 * 缓存的单词列表对象
		 */
		private final List<JSONWord> wordList;

		/**
		 * 构造方法
		 * <p>
		 * 需要提供一个词法分析器分析得来的单词列表
		 * 
		 * @param wList
		 *            单词列表
		 */
		public JSONSyntaxAnalyzer(List<JSONWord> wordList) {
			this.wordList = wordList;
		}

		/**
		 * JSONSyntaxProcessEnum: 句法分析进程
		 * <p>
		 * 描述了句法分析的进程, 每一步都有相应的操作
		 * 
		 * @author mura
		 *
		 */
		private static enum JSONSyntaxProcessEnum {
			objectStart, objectGetKey, objectGetColon, objectGetValue, arrayStart, arrayGetValue
		}

		/**
		 * 进行句法分析, 并且构建相应的JSON对象
		 * 
		 * @param root
		 *            结果写入此JSON对象中
		 * @throws JSONParseException
		 *             当分析出句法错误时, 抛出此异常
		 */
		public void syntaxAnalyse(JSONObject root) throws JSONParseException {
			// 括号匹配用栈, 检查括号的匹配
			Stack<JSONWord> bracketStack = new Stack<JSONWord>();
			// JSON集合(对象, 数组)用栈
			Stack<JSONCollection> collectionStack = new Stack<JSONCollection>();

			// 检查该单词列表的完整性, 是否是大括号开头和结尾
			if (wordList.size() < 2 || wordList.get(0).type != JSONWordTypeEnum.LBRACE
					|| wordList.get(wordList.size() - 1).type != JSONWordTypeEnum.RBRACE) {
				// 首尾不符合, 不是合法的JSON代码字符串
				parseError("JSON source should start with \"{\" and end with \"}\".");
			} else {
				// 首尾检查通过, 开始根据单词类型逐词分析
				JSONSyntaxProcessEnum process = JSONSyntaxProcessEnum.objectStart;

				// 压入起始大括号
				bracketStack.push(wordList.get(0));
				// 压入指示结束的对象
				JSONObject stackEnd = new JSONObject("end");
				collectionStack.push(stackEnd);
				// 压入根对象
				collectionStack.push(root);

				for (int i = 1; i < wordList.size(); i++) {
					JSONWord word = wordList.get(i);
					switch (process) {
					case objectStart:
						switch (word.type) {
						case STRING:
							// 读入一个作为键值的字符串, 进入下一轮循环, 等待冒号
							process = JSONSyntaxProcessEnum.objectGetKey;
							break;
						case RBRACE:
							// 读到右括号, 匹配括号栈
							if (bracketStack.pop().type != JSONWordTypeEnum.LBRACE) {
								// 括号匹配错误
								syntaxError(i);
							} else {
								// 括号匹配成功, 将当前的对象出栈, 检查栈内是否还有容器
								JSONCollection current = collectionStack.pop();
								if (collectionStack.peek() != stackEnd) {
									// 栈内还有其他容器, 将当前对象压入父容器中
									collectionStack.peek().add((JSONVariable) current);
									// 根据父容器对象的类型, 切换当前阶段
									if (collectionStack.peek().getClass() == JSONObject.class) {
										process = JSONSyntaxProcessEnum.objectGetValue;
									} else if (collectionStack.peek().getClass() == JSONArray.class) {
										process = JSONSyntaxProcessEnum.arrayGetValue;
									} else {
										syntaxError(i);
									}
								} else {
									// 已经弹出了根容器, 解析结束, 检查是否所有单词都已经解析完毕
									if (i < wordList.size() - 1) {
										// 并不是最后一个单词, 解析失败
										syntaxError(i);
									}
								}
							}
							break;
						default:
							syntaxError(i);
							break;
						}
						break;
					case objectGetKey:
						switch (word.type) {
						case COLON:
							// 读到冒号, 进入下一轮循环, 等待值
							process = JSONSyntaxProcessEnum.objectGetColon;
							break;
						default:
							syntaxError(i);
							break;
						}
						break;
					case objectGetColon:
						// 进入下一阶段
						process = JSONSyntaxProcessEnum.objectGetValue;
						// 根据读到的value类型, 建立新的变量对象
						switch (word.type) {
						case STRING:
							// 字符串类型值
							collectionStack.peek().add(new JSONString(former2(i).word, word.word));
							break;
						case INTEGER:
							// 整型值
							collectionStack.peek().add(new JSONInteger(former2(i).word, Integer.parseInt(word.word)));
							break;
						case FLOAT:
						case EXPONENT:
							collectionStack.peek().add(new JSONFloat(former2(i).word, Float.parseFloat(word.word)));
							break;
						case TRUE:
							collectionStack.peek().add(new JSONBoolean(former2(i).word, true));
							break;
						case FALSE:
							collectionStack.peek().add(new JSONBoolean(former2(i).word, false));
							break;
						case NULL:
							collectionStack.peek().add(new JSONBoolean(former2(i).word));
							break;
						case LBRACE:
							// 遇到左大括号, 说明这是一个新的Object
							collectionStack.push(new JSONObject(former2(i).word));
							process = JSONSyntaxProcessEnum.objectStart;
							bracketStack.push(word);
							break;
						case LBRACKET:
							// 遇到左方括号, 说明这是一个新的Array
							collectionStack.push(new JSONArray(former2(i).word));
							// 切换阶段, 进入ArrayStart阶段
							process = JSONSyntaxProcessEnum.arrayStart;
							bracketStack.push(word);
							break;
						default:
							syntaxError(i);
							break;
						}
						break;
					case objectGetValue:
						// 允许接收逗号或右括号
						switch (word.type) {
						case COMMA:
							// 读到逗号, 说明接下来还有键值对
							process = JSONSyntaxProcessEnum.objectStart;
							break;
						case RBRACE:
							// 读到右括号, 说明当前对象结束, 回滚一位, 处理对象结束
							process = JSONSyntaxProcessEnum.objectStart;
							i--;
							break;
						default:
							syntaxError(i);
							break;
						}
						break;
					case arrayStart:
						// 数组开始, 允许接收值或者右方括号作为结束
						process = JSONSyntaxProcessEnum.arrayGetValue;
						switch (word.type) {
						case STRING:
							collectionStack.peek().add(new JSONString(null, word.word));
							break;
						case INTEGER:
							collectionStack.peek().add(new JSONInteger(null, Integer.parseInt(word.word)));
							break;
						case FLOAT:
						case EXPONENT:
							collectionStack.peek().add(new JSONFloat(null, Float.parseFloat(word.word)));
							break;
						case TRUE:
							collectionStack.peek().add(new JSONBoolean(null, true));
							break;
						case FALSE:
							collectionStack.peek().add(new JSONBoolean(null, false));
							break;
						case NULL:
							collectionStack.peek().add(new JSONNull(null));
							break;
						case LBRACE:
							// 数组中有一个新的Object
							collectionStack.push(new JSONObject(null));
							process = JSONSyntaxProcessEnum.objectStart;
							bracketStack.push(word);
							break;
						case LBRACKET:
							// 数组中有一个新的Array
							collectionStack.push(new JSONArray(null));
							process = JSONSyntaxProcessEnum.arrayStart;
							bracketStack.push(word);
							break;
						case RBRACKET:
							// 数组结束, 匹配括号栈
							if (bracketStack.pop().type != JSONWordTypeEnum.LBRACKET) {
								// 括号匹配错误
								syntaxError(i);
							} else {
								// 括号匹配成功
								JSONCollection current = collectionStack.pop();
								if (collectionStack.peek() != stackEnd) {
									collectionStack.peek().add((JSONVariable) current);
									// 切换阶段
									if (collectionStack.peek().getClass() == JSONObject.class) {
										process = JSONSyntaxProcessEnum.objectGetValue;
									} else if (collectionStack.peek().getClass() == JSONArray.class) {
										process = JSONSyntaxProcessEnum.arrayGetValue;
									} else {
										syntaxError(i);
									}
								} else {
									// JSON的根容器应当是对象
									syntaxError(i);
								}
							}

							break;
						default:
							syntaxError(i);
							break;
						}
						break;
					case arrayGetValue:
						// 数组读到了一个值
						process = JSONSyntaxProcessEnum.arrayStart;
						switch (word.type) {
						case COMMA:
							break;
						case RBRACKET:
							// 回滚一位, 处理数组结束
							i--;
							break;
						default:
							syntaxError(i);
						}
						break;
					}
				}
			}
		}

		/**
		 * 报告一个语法错误, 并中断语法分析
		 * 
		 * @param i
		 *            索引号
		 * @throws JSONParseException
		 *             抛出异常结束分析
		 */
		private void syntaxError(int i) throws JSONParseException {
			JSONWord former2 = former2(i);
			JSONWord former = former(i);
			JSONWord word = former(i + 1);
			StringBuilder sb = new StringBuilder();
			sb.append("Syntax error near: \"");
			if (former2.type == JSONWordTypeEnum.STRING) {
				sb.append('\"');
				sb.append(former2.word);
				sb.append('\"');
			} else {
				sb.append(former2.word);
			}
			sb.append(' ');
			if (former.type == JSONWordTypeEnum.STRING) {
				sb.append('\"');
				sb.append(former.word);
				sb.append('\"');
			} else {
				sb.append(former.word);
			}
			sb.append(' ');
			if (word.type == JSONWordTypeEnum.STRING) {
				sb.append('\"');
				sb.append(word.word);
				sb.append('\"');
			} else {
				sb.append(word.word);
			}
			sb.append('\"');
			parseError(sb.toString());
		}

		/**
		 * 获取前一个的word
		 * 
		 * @param i
		 *            索引号
		 * @return 前前的word
		 */
		private JSONWord former(int i) {
			if (i > 1) {
				return wordList.get(i - 1);
			} else {
				return new JSONWord("null", JSONWordTypeEnum.NULL);
			}
		}

		/**
		 * 获取前前的word
		 * 
		 * @param i
		 *            索引号
		 * @return 前前的word
		 */
		private JSONWord former2(int i) {
			if (i > 2) {
				return wordList.get(i - 2);
			} else {
				return new JSONWord("null", JSONWordTypeEnum.NULL);
			}
		}
	}
}
