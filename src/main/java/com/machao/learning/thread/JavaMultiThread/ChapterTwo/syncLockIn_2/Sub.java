package com.machao.learning.thread.JavaMultiThread.ChapterTwo.syncLockIn_2;

public class Sub extends Main {
	public synchronized void operateISubMethod() {
		// TODO Auto-generated method stub
		while (i > 0) {
			try {
				i--;
				System.out.println("sub print i=" + i);
				Thread.sleep(100);
				this.operateIMainMethod();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
