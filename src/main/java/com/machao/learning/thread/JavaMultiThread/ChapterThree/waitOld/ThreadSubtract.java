package com.machao.learning.thread.JavaMultiThread.ChapterThree.waitOld;

public class ThreadSubtract extends Thread {
	private Subtract r;

	public ThreadSubtract(Subtract r) {
		super();
		this.r = r;
	}
	
	@Override
	public void run() {
		r.subtract();
	}
}
