package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t9;

public class Run {

	public static void main(String[] args) {
		PrintString printStringService = new PrintString();
		printStringService.printStringMethod();
		System.out.println("我要停止它！stopThread=" + Thread.currentThread().getName());
		printStringService.setContinuePrint(false);
	}

}
