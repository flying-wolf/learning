package com.machao.learning.thread.JavaMultiThread.ChapterTwo.volatileTestThread;

public class MyThread extends Thread {
	public static volatile int count;
	
	private static void addCount() {
		for (int i = 0; i < 100; i++) {
			count ++;
		}
		System.out.println("count=" + count);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		addCount();
	}
}
