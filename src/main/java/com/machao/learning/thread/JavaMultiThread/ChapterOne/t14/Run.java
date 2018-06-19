package com.machao.learning.thread.JavaMultiThread.ChapterOne.t14;

public class Run {

	public static void main(String[] args) {
		try {
			MyThread mythread = new MyThread();
			mythread.start();
			Thread.sleep(200);
			mythread.interrupt();
		} catch (InterruptedException e) {
			System.out.println("main catch");
			e.printStackTrace();
		}
		System.out.println("end");
	}

}
