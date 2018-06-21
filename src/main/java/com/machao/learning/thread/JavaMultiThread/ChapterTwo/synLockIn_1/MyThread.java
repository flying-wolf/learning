package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synLockIn_1;

public class MyThread extends Thread {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Service service = new Service();
		service.service1();
	}
}
