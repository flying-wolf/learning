package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synNotExtends;

public class Test {

	public static void main(String[] args) {
		Sub subRef = new Sub();
		ThreadA a = new ThreadA(subRef);
		a.setName("A");
		a.start();
		ThreadB b = new ThreadB(subRef);
		b.setName("B");
		b.start();
	}

}
