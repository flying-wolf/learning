package com.machao.learning.thread.JavaMultiThread.ChapterThree.join_sleep_1;

public class ThreadA extends Thread {
	private ThreadB b;

	public ThreadA(ThreadB b) {
		super();
		this.b = b;
	}

	@Override
	public void run() {
		try {
			synchronized (b) {
				b.start();
				Thread.sleep(6000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
