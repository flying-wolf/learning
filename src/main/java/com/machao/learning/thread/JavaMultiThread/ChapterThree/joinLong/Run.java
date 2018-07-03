package com.machao.learning.thread.JavaMultiThread.ChapterThree.joinLong;

public class Run {

	public static void main(String[] args) {
		try {
			MyThread threadTest = new MyThread();
			threadTest.start();
			//threadTest.join(2000);//只等2秒
			Thread.sleep(2000);
			System.out.println("  end Timer="+System.currentTimeMillis());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
