package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synchronizedOneThreadIn;

public class ObjectService {
	public void serviceMethod() {
		try {
			synchronized (this) {
				System.out.println("begin time=" + System.currentTimeMillis());
				Thread.sleep(2000);
				System.out.println("end   time=" + System.currentTimeMillis());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
