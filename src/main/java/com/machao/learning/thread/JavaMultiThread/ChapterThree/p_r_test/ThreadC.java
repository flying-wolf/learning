package com.machao.learning.thread.JavaMultiThread.ChapterThree.p_r_test;

public class ThreadC extends Thread {
	private C r;

	public ThreadC(C r) {
		super();
		this.r = r;
	}
	
	@Override
	public void run() {
		while(true) {
			r.getValue();
		}
	}
}
