package com.machao.learning.ChapterOne.CallMainMethodMainThread;

public class MyThread extends Thread {
	@Override
	public void run() {
		super.run();
		System.out.println("MyThread");
	}
}
