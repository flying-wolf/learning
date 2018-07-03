package com.machao.learning.thread.JavaMultiThread.ChapterFour.UseConditionWaitNotifyOK;

public class ThreadA extends Thread {
	private MyService service;

	public ThreadA(MyService service) {
		super();
		this.service = service;
	}

	@Override
	public void run() {
		service.await();
	}
}
