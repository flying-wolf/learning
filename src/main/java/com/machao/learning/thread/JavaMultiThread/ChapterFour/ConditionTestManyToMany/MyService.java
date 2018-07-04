package com.machao.learning.thread.JavaMultiThread.ChapterFour.ConditionTestManyToMany;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyService {
	private Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	private boolean hasValue = false;

	public void set() {
		try {
			lock.lock();
			while (hasValue) {
				System.out.println("有可能★★连续");
				condition.await();
			}
			System.out.println("打印★");
			hasValue = true;
			condition.signalAll();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void get() {
		try {
			lock.lock();
			while (!hasValue) {
				System.out.println("有可能☆☆连续");
				condition.await();
			}
			System.out.println("打印☆");
			hasValue = false;
			condition.signalAll();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
