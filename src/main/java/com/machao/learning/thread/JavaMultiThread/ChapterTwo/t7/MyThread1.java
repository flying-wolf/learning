package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t7;

public class MyThread1 extends Thread {
	private Task task;

	public MyThread1(Task task) {
		super();
		this.task = task;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		task.doLongTimeTask();
	}
}
