package com.machao.learning.ChapterOne.t1;

public class Test {

	public static void main(String[] args) {
		MyThread t1 = new MyThread(1);
		MyThread t2 = new MyThread(2);
		MyThread t3 = new MyThread(3);
		MyThread t4 = new MyThread(4);
		MyThread t5 = new MyThread(5);
		MyThread t6 = new MyThread(6);
		MyThread t7 = new MyThread(7);
		MyThread t8 = new MyThread(8);
		MyThread t9 = new MyThread(9);
		MyThread t10 = new MyThread(10);
		MyThread t11 = new MyThread(11);
		MyThread t12 = new MyThread(12);
		MyThread t13 = new MyThread(13);
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		t7.start();
		t8.start();
		t9.start();
		t10.start();
		t11.start();
		t12.start();
		t13.start();
	}

}
