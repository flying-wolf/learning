package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t8;

public class Task {
	public synchronized void otherMethod() {
		System.out.println("----------------------run--othreMethod");
	}
	
	public void doLongTimeTask() {
		synchronized (this) {
			for (int i = 0; i < 10000; i++) {
				System.out.println("synchronized threadName="
						+ Thread.currentThread().getName() + " i=" + (i + 1));
			}
		}
	}
}
