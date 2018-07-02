package com.machao.learning.thread.JavaMultiThread.ChapterThree.waitOld;

public class ThreadAdd extends Thread {
	private Add p;

	public ThreadAdd(Add p) {
		super();
		this.p = p;
	}
	
	@Override
	public void run() {
		p.add();
	}
}
