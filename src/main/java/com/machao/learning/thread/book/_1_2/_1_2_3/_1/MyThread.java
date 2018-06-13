package com.machao.learning.thread.book._1_2._1_2_3._1;

public class MyThread extends Thread {
	private int count = 5;

	public MyThread(String name) {
		super();
		this.setName(name);
	}
	
	@Override
	public void run() {
		while (this.count > 0) {
			this.count --;
			System.out.println("由"+this.getName()+"计算,count="+this.count);
			
		}
	}
	
}
