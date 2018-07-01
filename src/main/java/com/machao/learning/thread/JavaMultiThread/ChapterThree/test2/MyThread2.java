package com.machao.learning.thread.JavaMultiThread.ChapterThree.test2;

public class MyThread2 extends Thread {
	private Object lock;

	public MyThread2(Object lock) {
		super();
		this.lock = lock;
	}
	
	@Override
	public void run() {
		synchronized (lock) {
			System.out.println("开始     notify time="+System.currentTimeMillis());
			lock.notify();
			System.out.println("结束     notify time="+System.currentTimeMillis());
		}
	}
}
