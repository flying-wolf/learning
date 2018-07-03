package com.machao.learning.thread.JavaMultiThread.ChapterThree.joinTest1;

public class MyThread extends Thread {
	@Override
	public void run() {
		try {
			int secondValue = (int) (Math.random() * 10000);
			System.out.println(secondValue);
			Thread.sleep(secondValue);
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
