package edu.sysu.ncps.servlet.base;

/**
 * JSON格式Bean基类, 继承后使用<br>
 * 需要在Servlet的子类中自定义一个静态内部类, 必须继承自此类<br>
 * 需要定义为<b>public</b>不然不能被反射获取<br>
 * 只要定义一个, 即使定义多个也只有一个会起作用
 * <p>
 * 在子类中, 需要用成员变量定义将要返回的JSON键值对及其类型, 变量格式如下:<br>
 * 访问标识: public<br>
 * 变量名: 同键值对的键名<br>
 * 若为可选键值对, 在键名前加<b>'_'</b>作为前缀以示区分<br>
 * 必要键值对将一定被返回, 如果没有赋值时将会返回<b>null</b>, 可选键值对只在有值时返回
 * <p>
 * 类型关系表: (JSON -> Java)<br>
 * Integer -> <b>Integer</b><br>
 * Float -> <b>Float</b><br>
 * Bool -> <b>Boolean</b><br>
 * String -> <b>String</b><br>
 * Data -> <b>byte[]</b><br>
 * &lt;Any&gt;Array -> <b>List&lt;(上述类型)&gt;</b><br>
 * 定义其它类型的成员变量可能导致问题!
 * <p>
 * 变量命名举例:<br>
 * <b>private String</b> comment;<br>
 * 对应<b>String</b>类型的名为<b>comment</b>的必要键值对<br>
 * <b>private byte[]</b> _image;<br>
 * 对应<b>Data</b>类型的名为<b>image</b>的可选键值对
 * 
 * @author mura
 */
public abstract class BaseJSONBean {

}
