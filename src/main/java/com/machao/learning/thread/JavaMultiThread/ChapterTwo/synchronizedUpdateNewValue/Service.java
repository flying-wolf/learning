package com.machao.learning.thread.JavaMultiThread.ChapterTwo.synchronizedUpdateNewValue;

public class Service {
	private boolean isContinueRun = true;
	public void runMethod() {
		String anyString = new String();
		while(isContinueRun == true) {
			synchronized (anyString) {
			}
		}
		System.out.println("停下来了！");
	}
	public void stopMethod() {
		isContinueRun = false;
	}
}
