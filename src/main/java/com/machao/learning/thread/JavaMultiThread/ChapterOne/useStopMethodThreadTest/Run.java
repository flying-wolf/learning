package com.machao.learning.thread.JavaMultiThread.ChapterOne.useStopMethodThreadTest;

public class Run {

	public static void main(String[] args) {
		try {
			MyThread mythread = new MyThread();
			mythread.start();
			Thread.sleep(8000);
			mythread.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
