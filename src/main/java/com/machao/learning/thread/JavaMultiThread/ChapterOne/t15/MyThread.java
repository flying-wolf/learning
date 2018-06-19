package com.machao.learning.thread.JavaMultiThread.ChapterOne.t15;

public class MyThread extends Thread {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			for (int i = 0; i < 100000; i++) {
				System.out.println("i="+(i+1));
			}
			System.out.println("run begin");
			Thread.sleep(200000);
			System.out.println("run end");
		} catch (InterruptedException e) {
			System.out.println("先停止，在遇到sleep！进入catch！");
			e.printStackTrace();
		}
	}
}
