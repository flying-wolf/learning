package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t6;

public class Run {

	public static void main(String[] args) {
		Task task = new Task();
		MyThread1 t1 = new MyThread1(task);
		t1.start();
		MyThread2 t2 = new MyThread2(task);
		t2.start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		long beginTime = Math.min(CommonUtils.beginTime1, CommonUtils.beginTime2);
		long endTime = Math.max(CommonUtils.endTime1, CommonUtils.endTime2);
		System.out.println("耗时：" + ((endTime - beginTime) / 1000));
	}

}
