package com.machao.learning.thread.JavaMultiThread.ChapterThree.p_c_allWait_fix;

public class Run {

	public static void main(String[] args) throws InterruptedException {
		String lock = new String("");
		P p = new P(lock);
		C r = new C(lock);
		ThreadP[] pThread = new ThreadP[2];
		ThreadC[] rThread = new ThreadC[2];
		for (int i = 0; i < 2; i++) {
			pThread[i] = new ThreadP(p);
			pThread[i].setName("生产者"+(i+1));
			rThread[i] = new ThreadC(r);
			rThread[i].setName("消费者"+(i+1));
			pThread[i].start();
			rThread[i].start();
		}
		Thread.sleep(5000);
		Thread[] threadArray = new Thread[Thread.currentThread().getThreadGroup().activeCount()];
		Thread.currentThread().getThreadGroup().enumerate(threadArray);
		for (Thread thread : threadArray) {
			System.out.println(thread.getName()+" "+thread.getState());
		}
	}

}
