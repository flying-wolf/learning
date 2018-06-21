package com.machao.learning.thread.JavaMultiThread.ChapterTwo.syncLockIn_2;

public class MyThread extends Thread {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Sub sub = new Sub();
		sub.operateISubMethod();
	}
}
