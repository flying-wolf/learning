package com.machao.learning.thread.JavaMultiThread.ChapterOne.t15;

public class Run {

	public static void main(String[] args) {
		MyThread mythread = new MyThread();
		mythread.start();
		mythread.interrupt();
		System.out.println("end");
	}

}
