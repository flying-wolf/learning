package com.machao.learning.thread.JavaMultiThread.ChapterThree.joinMoreTest;

public class ThreadB extends Thread {
	@Override
	public synchronized void run() {
		try {
			System.out.println(
					"begin B ThreadName=" + Thread.currentThread().getName() + "  " + System.currentTimeMillis());
			Thread.sleep(5000);
			System.out.println(
					"  end B ThreadName=" + Thread.currentThread().getName() + "  " + System.currentTimeMillis());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
