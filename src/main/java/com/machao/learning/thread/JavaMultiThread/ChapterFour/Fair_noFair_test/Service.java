package com.machao.learning.thread.JavaMultiThread.ChapterFour.Fair_noFair_test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Service {
	private Lock lock;

	public Service(boolean isFair) {
		super();
		this.lock = new ReentrantLock(isFair);
	}

	public void serviceMethod() {
		try {
			lock.lock();
			System.out.println("ThreadName=" + Thread.currentThread().getName() + "获得锁定");
		} finally {
			lock.unlock();
		}
	}
}
