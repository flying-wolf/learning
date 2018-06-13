package com.machao.learning.thread.book._1_2._1_2_2._1;

public class Run {

	public static void main(String[] args) {
		MyRunnable myrunnable = new MyRunnable();
		Thread thread = new Thread(myrunnable);
		thread.start();
		System.out.println("运行结束！");
	}

}
