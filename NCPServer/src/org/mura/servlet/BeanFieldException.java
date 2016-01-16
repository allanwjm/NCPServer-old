package org.mura.servlet;

/**
 * 请求参数Bean或JSON格式Bean成员变量错误而引发的异常
 * 
 * @author mura
 */
public class BeanFieldException extends Exception {

	private static final long serialVersionUID = 1L;

	public BeanFieldException(String msg) {
		super(msg);
	}
}
