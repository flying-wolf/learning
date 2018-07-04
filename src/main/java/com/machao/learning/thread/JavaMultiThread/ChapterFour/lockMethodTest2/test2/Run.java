package com.machao.learning.thread.JavaMultiThread.ChapterFour.lockMethodTest2.test2;

public class Run {

	public static void main(String[] args) throws InterruptedException {
		final Service service = new Service();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				service.waitMethod();
			}
		};

		Thread[] threadArray = new Thread[10];
		for (int i = 0; i < threadArray.length; i++) {
			threadArray[i] = new Thread(runnable);
		}
		for (int i = 0; i < threadArray.length; i++) {
			threadArray[i].start();
		}
		Thread.sleep(2000);
		service.notifyMethod();
	}

}
