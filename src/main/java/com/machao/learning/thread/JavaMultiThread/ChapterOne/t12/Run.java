package com.machao.learning.thread.JavaMultiThread.ChapterOne.t12;

public class Run {

	public static void main(String[] args) {
		try {
			MyThread mythread = new MyThread();
			mythread.start();
			Thread.sleep(1000);
			mythread.interrupt();
			System.out.println("是否停止1 ？"+mythread.interrupted());
			System.out.println("是否停止2 ？"+mythread.interrupted());
		} catch (InterruptedException e) {
			System.out.println("main cache");
			e.printStackTrace();
		}
		System.out.println("end");
	}

}
