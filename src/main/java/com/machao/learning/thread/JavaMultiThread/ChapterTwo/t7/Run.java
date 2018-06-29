package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t7;

public class Run {

	public static void main(String[] args) {
		Task task = new Task();
		MyThread1 t1 = new MyThread1(task);
		t1.start();
		MyThread2 t2 = new MyThread2(task);
		t2.start();
	}

}
