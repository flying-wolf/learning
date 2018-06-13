package com.machao.learning.thread.book._1_2._1_2_1._1;

public class Run {

	public static void main(String[] args) {
		MyThread mythread = new MyThread();
		mythread.start();
		System.out.println("运行结束！");
	}

}
