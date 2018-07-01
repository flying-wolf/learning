package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t10;

public class Run {

	public static void main(String[] args) {
		PrintString printStringService = new PrintString();
		new Thread(printStringService).start();
		System.out.println("我要停止它！stopThread=" + Thread.currentThread().getName());
		printStringService.setContinuePrint(false);
	}

}
