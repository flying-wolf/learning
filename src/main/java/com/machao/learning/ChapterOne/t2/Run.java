package com.machao.learning.ChapterOne.t2;

public class Run {

	public static void main(String[] args) {
		MyRunnable myrunnable = new MyRunnable();
		Thread thread = new Thread(myrunnable);
		thread.start();
		System.out.println("运行结束！");
	}

}
