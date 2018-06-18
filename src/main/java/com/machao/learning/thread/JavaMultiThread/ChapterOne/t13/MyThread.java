package com.machao.learning.thread.JavaMultiThread.ChapterOne.t13;

public class MyThread extends Thread {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		for (int i = 0; i < 500000; i++) {
			if(this.interrupted()) {
				System.out.println("已经是停止状态了！我要退出！");
				break;
			}
			System.out.println("i="+(i+1));
		}
	}
}
