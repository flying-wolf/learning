package com.machao.learning.thread.JavaMultiThread.ChapterTwo.doubleSynBlockOneTwo;

public class Run {

	public static void main(String[] args) {
		ObjectService service = new ObjectService();
		ThreadA a = new ThreadA(service);
		a.start();
		ThreadB b = new ThreadB(service);
		b.start();
	}

}
