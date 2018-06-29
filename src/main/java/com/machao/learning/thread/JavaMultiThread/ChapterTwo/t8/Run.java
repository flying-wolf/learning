package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t8;

public class Run {

	public static void main(String[] args) throws InterruptedException {
		Task task = new Task();
		MyThread1 t1 = new MyThread1(task);
		t1.start();
		Thread.sleep(100);
		MyThread2 t2 = new MyThread2(task);
		t2.start();
	}

}
