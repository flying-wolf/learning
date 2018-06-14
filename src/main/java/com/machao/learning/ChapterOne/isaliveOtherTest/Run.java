package com.machao.learning.ChapterOne.isaliveOtherTest;

public class Run {

	public static void main(String[] args) {
		CountOperate c = new CountOperate();
		Thread t = new Thread(c);
		System.out.println("main begin isAlive() ==" + t.isAlive());
		t.setName("A");
		t.start();
		System.out.println("main end isAlive() ==" + t.isAlive());

	}

}
