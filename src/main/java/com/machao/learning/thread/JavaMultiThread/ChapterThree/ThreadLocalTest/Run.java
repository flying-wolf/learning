package com.machao.learning.thread.JavaMultiThread.ChapterThree.ThreadLocalTest;

public class Run {

	public static void main(String[] args) {
		try {
			ThreadA a = new ThreadA();
			ThreadB b = new ThreadB();
			a.start();
			b.start();
			for (int i = 0; i < 100; i++) {
				Tools.t1.set("Main" + (i + 1));
				System.out.println("Main get Value=" + Tools.t1.get());
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
