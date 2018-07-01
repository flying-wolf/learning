package com.machao.learning.thread.JavaMultiThread.ChapterThree.notifyAll;

public class ThreadC extends Thread {
	private Object lock;

	public ThreadC(Object lock) {
		super();
		this.lock = lock;
	}
	
	@Override
	public void run() {
		Service service = new Service();
		service.testMethod(lock);
	}
}
