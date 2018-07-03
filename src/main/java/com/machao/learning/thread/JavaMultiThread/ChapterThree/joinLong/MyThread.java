package com.machao.learning.thread.JavaMultiThread.ChapterThree.joinLong;

public class MyThread extends Thread {
	@Override
	public void run() {
		try {
			System.out.println("begin Timer=" + System.currentTimeMillis());
			sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
