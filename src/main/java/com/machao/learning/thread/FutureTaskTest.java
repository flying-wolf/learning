package com.machao.learning.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

public class FutureTaskTest extends FutureTask<String> {

	public FutureTaskTest(final CountDownLatch cdl) {
		super(new Callable<String>() {

			@Override
			public String call() throws Exception {
				try {
					return getValue();
				} finally {
					cdl.countDown();
				}
			}
		});
	}
	
	public static String getValue() {
		String result = Thread.currentThread().getName();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
