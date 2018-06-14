package com.machao.learning.thread.JavaMultiThread.ChapterOne.runThread;

public class Test {

	public static void main(String[] args) {
		Thread runThread = Thread.currentThread();
		System.out.println(runThread.getName() + "  " + runThread.getId());

	}

}
