package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synchronizedOneThreadIn;

public class ThreadA extends Thread {
	private ObjectService service;

	public ThreadA(ObjectService service) {
		super();
		this.service = service;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		service.serviceMethod();
	}
}
