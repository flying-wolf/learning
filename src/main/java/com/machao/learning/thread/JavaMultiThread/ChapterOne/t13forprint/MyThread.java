package com.machao.learning.thread.JavaMultiThread.ChapterOne.t13forprint;

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
		
		System.out.println("我被输出，如果此代码是for又继续运行，线程并未停止！");
	}
}
