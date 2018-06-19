package com.machao.learning.thread.JavaMultiThread.ChapterOne.t14;

public class MyThread extends Thread {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			System.out.println("run begin");
			Thread.sleep(200000);
			System.out.println("run end");
		} catch (InterruptedException e) {
			System.out.println("在沉睡中被停止！进入catch！"+this.isInterrupted());
			e.printStackTrace();
		}
	}
}
