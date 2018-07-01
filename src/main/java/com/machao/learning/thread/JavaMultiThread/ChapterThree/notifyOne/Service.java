package com.machao.learning.thread.JavaMultiThread.ChapterThree.notifyOne;

public class Service {
	public void testMethod(Object lock) {
		try {
			synchronized (lock) {
				System.out.println("begin wait() ThreadName=" + Thread.currentThread().getName());
				lock.wait();
				System.out.println("end wait() ThreadName=" + Thread.currentThread().getName());
			}
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
