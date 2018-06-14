package com.machao.learning.ChapterOne.t4;

public class MyThread extends Thread {
	private int count = 5;
	@Override
	synchronized public void run() {
		count --;
		System.out.println("由"+this.currentThread().getName()+"计算,count="+this.count);
	}
}
