package com.machao.learning.thread.JavaMultiThread.ChapterTwo.twoObjectTwoLock;

public class Run {

	public static void main(String[] args) {
		HasSelfPrivateNum numRef1 = new HasSelfPrivateNum();
		HasSelfPrivateNum numRef2 = new HasSelfPrivateNum();
		ThreadA threadA = new ThreadA(numRef1);
		threadA.start();
		ThreadB threadB = new ThreadB(numRef2);
		threadB.start();
	}

}
