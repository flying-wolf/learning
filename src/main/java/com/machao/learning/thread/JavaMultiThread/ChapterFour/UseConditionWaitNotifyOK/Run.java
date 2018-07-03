package com.machao.learning.thread.JavaMultiThread.ChapterFour.UseConditionWaitNotifyOK;

public class Run {

	public static void main(String[] args) {
		try {
			MyService service = new MyService();
			ThreadA a = new ThreadA(service);
			a.start();
			Thread.sleep(3000);
			service.signal();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
