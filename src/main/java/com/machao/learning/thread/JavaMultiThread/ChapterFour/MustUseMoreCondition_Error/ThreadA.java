package com.machao.learning.thread.JavaMultiThread.ChapterFour.MustUseMoreCondition_Error;

public class ThreadA extends Thread {
	private MyService service;

	public ThreadA(MyService service) {
		super();
		this.service = service;
	}

	@Override
	public void run() {
		service.awaitA();
	}

}
