package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synNotExtends;

public class ThreadA extends Thread {
	private Sub sub;
	
	public ThreadA(Sub sub) {
		super();
		this.sub = sub;
	}

	@Override
	public void run() {
		sub.serviceMethod();
	}
}
