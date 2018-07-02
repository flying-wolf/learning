package com.machao.learning.thread.JavaMultiThread.ChapterThree.p_c_allWait_fix;

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
