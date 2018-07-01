package com.machao.learning.thread.JavaMultiThread.ChapterTwo.atomicIntegerNoSafe;

public class MyThread extends Thread {
	private MyService service;

	public MyThread(MyService service) {
		super();
		this.service = service;
	}
	
	@Override
	public void run() {
		service.addNum();
	}
}
