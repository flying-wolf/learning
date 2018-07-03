package com.machao.learning.thread.JavaMultiThread.ChapterThree.InheritableThreadLocal1;

public class ThreadA extends Thread {
	@Override
	public void run() {
		try {
			for (int i = 0; i < 10; i++) {
				System.out.println("在ThreadA线程中取值" + Tools.t1.get());
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
