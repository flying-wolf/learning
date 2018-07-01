package com.machao.learning.thread.JavaMultiThread.ChapterThree.waitHasParamMethod;

public class MyRunnable {
	private static Object lock = new Object();
	private static Runnable runnable1 = new Runnable() {

		@Override
		public void run() {
			try {
				synchronized (lock) {
					System.out.println("wait begin timer=" + System.currentTimeMillis());
					lock.wait(5000);
					System.out.println("wait   end timer=" + System.currentTimeMillis());
				}
			} catch (InterruptedException e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	};
	
	private static Runnable runnable2 = new Runnable() {

		@Override
		public void run() {
			synchronized (lock) {
				System.out.println("notify begin timer=" + System.currentTimeMillis());
				lock.notify();
				System.out.println("notify   end timer=" + System.currentTimeMillis());
			}
		}
	};
	
	public static void main(String[] args) {
		try {
			Thread t1 = new Thread(runnable1);
			t1.start();
			Thread.sleep(3000);
			Thread t2 = new Thread(runnable2);
			t2.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
