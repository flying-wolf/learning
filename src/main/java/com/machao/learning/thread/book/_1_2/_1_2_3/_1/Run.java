package com.machao.learning.thread.book._1_2._1_2_3._1;

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
