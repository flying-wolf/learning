package com.machao.learning.thread.JavaMultiThread.ChapterTwo.atomicIntegerNoSafe;

import java.util.concurrent.atomic.AtomicInteger;

public class MyService {
	public static AtomicInteger aiRef = new AtomicInteger();
	public synchronized void addNum() {
		System.out.println(Thread.currentThread().getName()+"加了100之后的值是："
				+ aiRef.addAndGet(100));
		aiRef.addAndGet(1);
	}
}
