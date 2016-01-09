package edu.sysu.ncps;

public class Test {

	public static void main(String[] args) {
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
		for (StackTraceElement e : s) {
			System.out.println(e);
		}
	}

}
