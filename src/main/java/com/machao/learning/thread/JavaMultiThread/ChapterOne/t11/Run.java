package com.machao.learning.thread.JavaMultiThread.ChapterOne.t11;

public class Run {

	public static void main(String[] args) {
		try {
			MyThread mythread = new MyThread();
			mythread.start();
			Thread.sleep(2000);
			mythread.interrupt();
		} catch (InterruptedException e) {
			System.out.println("main cache");
			e.printStackTrace();
		}

	}

}
