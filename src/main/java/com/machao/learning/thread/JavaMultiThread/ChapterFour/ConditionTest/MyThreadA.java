package com.machao.learning.thread.JavaMultiThread.ChapterFour.ConditionTest;

public class MyThreadA extends Thread {
	private MyService service;

	public MyThreadA(MyService service) {
		super();
		this.service = service;
	}

	@Override
	public void run() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			service.set();
		}
	}
}
