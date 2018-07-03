package com.machao.learning.thread.JavaMultiThread.ChapterFour.ConditionTestMoreMethod;

public class ThreadAA extends Thread {
	private MyService service;

	public ThreadAA(MyService service) {
		super();
		this.service = service;
	}

	@Override
	public void run() {
		service.methodA();
	}
}
