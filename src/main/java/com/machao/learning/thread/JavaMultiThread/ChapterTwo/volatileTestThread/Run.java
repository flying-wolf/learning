package com.machao.learning.thread.JavaMultiThread.ChapterTwo.volatileTestThread;

public class Run {

	public static void main(String[] args) {
		MyThread[] mythreadArray = new MyThread[100];
		for (int i = 0; i < mythreadArray.length; i++) {
			mythreadArray[i] = new MyThread();
		}
		for (int i = 0; i < mythreadArray.length; i++) {
			mythreadArray[i].start();
		}

	}

}
