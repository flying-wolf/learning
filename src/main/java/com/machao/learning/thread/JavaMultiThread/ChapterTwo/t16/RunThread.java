package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t16;

public class RunThread extends Thread {
	private volatile boolean isRunning = true;

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	@Override
	public void run() {
		System.out.println("进入run了");
		while(isRunning() == true) {
			
		}
		System.out.println("线程被停止了！");
	}
}
