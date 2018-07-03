package com.machao.learning.thread.JavaMultiThread.ChapterThree.stack_3;

public class Run {

	public static void main(String[] args) {
		MyStack stack = new MyStack();
		P p1 = new P(stack);
		P p2 = new P(stack);
		P p3 = new P(stack);
		P p4 = new P(stack);
		P p5 = new P(stack);
		P p6 = new P(stack);
		P_Thread pt1 = new P_Thread(p1);
		P_Thread pt2 = new P_Thread(p2);
		P_Thread pt3 = new P_Thread(p3);
		P_Thread pt4 = new P_Thread(p4);
		P_Thread pt5 = new P_Thread(p5);
		P_Thread pt6 = new P_Thread(p6);
		pt1.start();
		pt2.start();
		pt3.start();
		pt4.start();
		pt5.start();
		pt6.start();
		C r1 = new C(stack);
		C_Thread rt1 = new C_Thread(r1);
		rt1.start();
	}

}
