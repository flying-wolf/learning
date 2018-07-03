package com.machao.learning.thread.JavaMultiThread.ChapterFour.MustUseMoreCondition_Error;

public class Run {

	public static void main(String[] args) {
		try {
			MyService service = new MyService();
			ThreadA a = new ThreadA(service);
			a.setName("A");
			a.start();
			ThreadB b = new ThreadB(service);
			b.setName("B");
			b.start();
			Thread.sleep(3000);
			service.signalAll();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
