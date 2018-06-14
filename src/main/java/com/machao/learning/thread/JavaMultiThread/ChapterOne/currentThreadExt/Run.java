package com.machao.learning.thread.JavaMultiThread.ChapterOne.currentThreadExt;

public class Run {

	public static void main(String[] args) {
		CountOperate c = new CountOperate();
		Thread t = new Thread(c);
		t.setName("A");
		t.start();

	}

}
