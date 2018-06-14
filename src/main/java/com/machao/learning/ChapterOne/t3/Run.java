package com.machao.learning.ChapterOne.t3;

public class Run {

	public static void main(String[] args) {
		MyThread t1 = new MyThread("A");
		MyThread t2 = new MyThread("B");
		MyThread t3 = new MyThread("C");
		t1.start();
		t2.start();
		t3.start();

	}

}
