package edu.sysu.ncps.servlet.base;

/**
 * 请求参数Bean或JSON格式Bean成员变量错误而引发的异常
 * 
 * @author mura
 */
public class InvalidBeanFieldException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidBeanFieldException(String msg) {
		super(msg);
	}
}
