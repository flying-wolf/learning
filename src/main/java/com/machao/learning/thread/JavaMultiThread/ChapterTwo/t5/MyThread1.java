package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t5;

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
		CommonUtils.beginTime1 = System.currentTimeMillis();
		task.doLongTimeTask();
		CommonUtils.endTime1 = System.currentTimeMillis();
	}
}
