package com.machao.learning.thread.JavaMultiThread.ChapterThree.firstNotify;

public class MyRun {
	private String lock = new String("");
	private Runnable runnableA = new Runnable() {
		@Override
		public void run() {
			synchronized (lock) {
				try {
					System.out.println("begin wait");
					lock.wait();
					System.out.println("  end wait");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	private Runnable runnableB = new Runnable() {
		
		@Override
		public void run() {
			synchronized (lock) {
				System.out.println("begin notify");
				lock.notify();
				System.out.println("  end notify");
			}
		}
	};
	
	public static void main(String[] args) {
		try {
			MyRun run = new MyRun();
			Thread b = new Thread(run.runnableB);
			b.start();
			Thread.sleep(100);
			Thread a = new Thread(run.runnableA);
			a.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
