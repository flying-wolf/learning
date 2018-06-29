package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synchronizedOneThreadIn;

public class ThreadB extends Thread {
	private ObjectService service;

	public ThreadB(ObjectService service) {
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
