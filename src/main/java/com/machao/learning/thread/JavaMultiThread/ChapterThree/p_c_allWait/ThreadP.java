package com.machao.learning.thread.JavaMultiThread.ChapterThree.p_c_allWait;

public class ThreadP extends Thread {
	private P p;

	public ThreadP(P p) {
		super();
		this.p = p;
	}
	
	@Override
	public void run() {
		while(true) {
			p.setValue();
		}
	}
}
