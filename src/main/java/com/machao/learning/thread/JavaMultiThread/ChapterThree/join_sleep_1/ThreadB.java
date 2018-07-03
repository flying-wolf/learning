package com.machao.learning.thread.JavaMultiThread.ChapterThree.join_sleep_1;

public class ThreadB extends Thread {
	@Override
	public void run() {
		try {
			System.out.println("  b run begin timer=" + System.currentTimeMillis());
			Thread.sleep(5000);
			System.out.println("  b run   end timer=" + System.currentTimeMillis());
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public synchronized void bService() {
		System.out.println("打印了bService timer=" + System.currentTimeMillis());
	}
}
