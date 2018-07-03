package com.machao.learning.thread.JavaMultiThread.ChapterThree.join_sleep_2;

public class ThreadC extends Thread {
	private ThreadB b;

	public ThreadC(ThreadB b) {
		super();
		this.b = b;
	}

	@Override
	public void run() {
		b.bService();
	}
}
