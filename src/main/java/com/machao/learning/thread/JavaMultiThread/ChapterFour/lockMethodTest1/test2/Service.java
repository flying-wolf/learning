package com.machao.learning.thread.JavaMultiThread.ChapterFour.lockMethodTest1.test2;

import java.util.concurrent.locks.ReentrantLock;

public class Service {
	public ReentrantLock lock = new ReentrantLock();

	public void serviceMethod1() {
		try {
			lock.lock();
			System.out.println("ThreadName=" + Thread.currentThread().getName() + "进入方法！");
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
