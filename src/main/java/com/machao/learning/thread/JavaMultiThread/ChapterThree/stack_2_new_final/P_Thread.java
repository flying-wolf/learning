package com.machao.learning.thread.JavaMultiThread.ChapterThree.stack_2_new_final;

public class P_Thread extends Thread {
	private P p;

	public P_Thread(P p) {
		super();
		this.p = p;
	}

	@Override
	public void run() {
		while (true) {
			p.pushService();
		}
	}
}
