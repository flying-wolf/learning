package com.machao.learning.ChapterOne.t4;

public class Run {

	public static void main(String[] args) {
		MyThread myThread = new MyThread();
		Thread t1 = new Thread(myThread, "A");
		Thread t2 = new Thread(myThread, "B");
		Thread t3 = new Thread(myThread, "C");
		Thread t4 = new Thread(myThread, "D");
		Thread t5 = new Thread(myThread, "E");
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();

	}

}
