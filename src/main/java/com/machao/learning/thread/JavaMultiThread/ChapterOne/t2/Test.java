package com.machao.learning.thread.JavaMultiThread.ChapterOne.t2;

public class Test {

	public static void main(String[] args) {
		try {
			MyThread mythread = new MyThread();
			mythread.setName("MyThread");
			mythread.start();
			for (int i = 0; i < 10; i++) {
				int time = (int) (Math.random() * 1000);
				Thread.sleep(time);
				System.out.println("run="+Thread.currentThread().getName());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
