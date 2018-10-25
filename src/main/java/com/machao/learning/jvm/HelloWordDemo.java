package com.machao.learning.jvm;

import java.io.File;

public class HelloWordDemo {
	
	private final int i = 0;
	private static int k = 0;
	
	private Object obj = new Object();
	
	private int sss = 0;
	
	public void methodOne(int i) {
		int j = 0;
		int sum = i + j;
		Object acb = obj;
		long start = System.currentTimeMillis();
		methodTwo();
		return;
	}
	
	public void methodTwo() {
		File file = new File("");
		// TODO Auto-generated method stub

	}
	
	public static void methodThree() {
		methodThree();
	}
	
	public static void main(String[] args) {
		methodThree();
	}

}
