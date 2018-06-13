package com.machao.learning.thread.book._1_2._1_2_1._3;

public class MyThread extends Thread {
	private int i;
	
	public MyThread(int i) {
		super();
		this.i = i;
	}

	@Override
	public void run() {
		System.out.println(i);
	}
}
