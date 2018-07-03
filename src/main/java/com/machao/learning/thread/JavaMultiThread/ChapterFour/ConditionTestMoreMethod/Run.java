package com.machao.learning.thread.JavaMultiThread.ChapterFour.ConditionTestMoreMethod;

public class Run {

	public static void main(String[] args) {
		try {
			MyService service = new MyService();
			ThreadA a = new ThreadA(service);
			a.setName("A");
			a.start();
			ThreadA aa = new ThreadA(service);
			aa.setName("AA");
			aa.start();
			Thread.sleep(100);
			ThreadA b = new ThreadA(service);
			b.setName("B");
			b.start();
			ThreadA bb = new ThreadA(service);
			bb.setName("BB");
			bb.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
