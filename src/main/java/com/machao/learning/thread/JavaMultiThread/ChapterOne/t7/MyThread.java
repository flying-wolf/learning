package com.machao.learning.thread.JavaMultiThread.ChapterOne.t7;

public class MyThread extends Thread {
	@Override
	public void run() {
		System.out.println("run=" + this.isAlive());
	}
}
