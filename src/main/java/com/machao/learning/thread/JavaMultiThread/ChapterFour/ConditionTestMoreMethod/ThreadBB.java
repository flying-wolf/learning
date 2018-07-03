package com.machao.learning.thread.JavaMultiThread.ChapterFour.ConditionTestMoreMethod;

public class ThreadBB extends Thread {
	private MyService service;

	public ThreadBB(MyService service) {
		super();
		this.service = service;
	}

	@Override
	public void run() {
		service.methodB();
	}
}
