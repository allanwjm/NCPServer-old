package edu.sysu.ncps;

import java.lang.reflect.Field;

public class Test {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Field[] a = A.class.getFields();
		Field[] ad = A.class.getDeclaredFields();
		Field[] b = B.class.getFields();
		Field[] bd = B.class.getDeclaredFields();
		funcA();
	}
	
	public static void funcA() {
		funcB();
	}
	
	public static void funcB() {
		funcC();
	}
	
	public static void funcC() {
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		for(StackTraceElement e : s) {
			System.out.println(e);
		}
	}

}
