package com.machao.learning.thread.JavaMultiThread.ChapterThree.stack_1;

public class Run {

	public static void main(String[] args) {
		MyStack stack = new MyStack();
		P p = new P(stack);
		C r = new C(stack);
		P_Thread pt = new P_Thread(p);
		C_Thread rt = new C_Thread(r);
		pt.start();
		rt.start();
	}

}
