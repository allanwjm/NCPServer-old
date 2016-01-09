package edu.sysu.ncps;

import java.lang.reflect.Field;

public class Test {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Field[] a = A.class.getFields();
		Field[] ad = A.class.getDeclaredFields();
		Field[] b = B.class.getFields();
		Field[] bd = B.class.getDeclaredFields();
	}

}
