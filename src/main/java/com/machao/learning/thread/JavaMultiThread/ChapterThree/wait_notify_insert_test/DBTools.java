package com.machao.learning.thread.JavaMultiThread.ChapterThree.wait_notify_insert_test;

public class DBTools {
	private volatile boolean prevIsA = false;

	public synchronized void backupA() {
		try {
			while (prevIsA) {
				wait();
			}
			for (int i = 0; i < 5; i++) {
				System.out.println("★★★★★");
			}
			prevIsA = true;
			notifyAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void backupB() {
		try {
			while (!prevIsA) {
				wait();
			}
			for (int i = 0; i < 5; i++) {
				System.out.println("☆☆☆☆☆");
			}
			prevIsA = false;
			notifyAll();
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
