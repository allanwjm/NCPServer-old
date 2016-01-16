package org.mura.servlet;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 请求参数Bean基类, 继承后使用<br>
 * 需要在Servlet的子类中自定义一个静态内部类, 必须继承自此类<br>
 * 需要定义为<b>public</b>不然不能被反射获取<br>
 * 只要定义一个, 即使定义多个也只有一个会起作用
 * <p>
 * 在子类中, 需要用成员变量定义从客户端发来的参数及其类型, 变量格式如下:<br>
 * 访问标识: public<br>
 * 变量名: 同请求参数的键名<br>
 * 若为可选参数, 在键名前加<b>'_'</b>作为前缀以示区分<br>
 * 在请求中出现了而在<b>Servlet</b>没有处理的参数, 不会影响执行, 不能被获取(想要获取, 请设置为可选变量)
 * <p>
 * 类型关系表: (JSON -> Java)<br>
 * Integer -> <b>Integer</b><br>
 * Float -> <b>Float</b><br>
 * Bool -> <b>Boolean</b><br>
 * String -> <b>String</b><br>
 * Data -> <b>byte[]</b><br>
 * &lt;Any&gt;Array -> <b>List&lt;String&gt;</b><br>
 * (!)由于泛型限制, 数组只能保存字符串值, 需要取出后自行进行类型转换<br>
 * 定义其它类型的成员变量可能导致问题!
 * <p>
 * 变量命名举例:<br>
 * <b>public String</b> comment;<br>
 * 对应<b>String</b>类型的名为<b>comment</b>的必要参数<br>
 * <b>public byte[]</b> _image;<br>
 * 对应<b>Data</b>类型的名为<b>image</b>的可选参数
 * 
 * @author mura
 */
public abstract class ParaBean {

	/**
	 * 将字符串List转换为整型List
	 * 
	 * @param list
	 *            字符串List
	 * @return 整型List
	 */
	public static List<Integer> convertIntegerList(List<String> list) {
		List<Integer> tList = new ArrayList<Integer>();
		for (String str : list) {
			tList.add(Integer.parseInt(str));
		}
		return tList;
	}

	/**
	 * 将字符串List转换为浮点型List
	 * 
	 * @param list
	 *            字符串List
	 * @return 浮点型List
	 */
	public static List<Float> convertFloatList(List<String> list) {
		List<Float> tList = new ArrayList<Float>();
		for (String str : list) {
			tList.add(Float.parseFloat(str));
		}
		return tList;
	}

	/**
	 * 将字符串List转换为布尔型List
	 * 
	 * @param list
	 *            字符串List
	 * @return 布尔型List
	 */
	public static List<Boolean> convertBoolList(List<String> list) {
		List<Boolean> tList = new ArrayList<Boolean>();
		for (String str : list) {
			tList.add(Boolean.parseBoolean(str));
		}
		return tList;
	}

	/**
	 * 将字符串List转换为二进制型List
	 * 
	 * @param list
	 *            字符串List
	 * @return 二进制型List
	 */
	public static List<byte[]> convertDataList(List<String> list) {
		List<byte[]> tList = new ArrayList<byte[]>();
		for (String str : list) {
			tList.add(Base64.getDecoder().decode(str));
		}
		return tList;
	}

}