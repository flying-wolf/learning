package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synchronizedMethodLockObject2;

public class MyObject {
	synchronized public void methodA() {
		// TODO Auto-generated method stub
		try {
			System.out.println("begin methodA threadName="+Thread.currentThread().getName());
			Thread.sleep(5000);
			System.out.println("end endTime="+System.currentTimeMillis());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	synchronized public void methodB() {
		// TODO Auto-generated method stub
		try {
			System.out.println("begin methodB threadName="+Thread.currentThread().getName()+" begin time="+System.currentTimeMillis());
			Thread.sleep(5000);
			System.out.println("end");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
