package com.machao.learning.thread.JavaMultiThread.ChapterThree.stack_2_new;

public class Run {

	public static void main(String[] args) {
		MyStack stack = new MyStack();
		P p = new P(stack);
		C r1 = new C(stack);
		C r2 = new C(stack);
		C r3 = new C(stack);
		C r4 = new C(stack);
		C r5 = new C(stack);
		P_Thread pt = new P_Thread(p);
		C_Thread rt1 = new C_Thread(r1);
		C_Thread rt2 = new C_Thread(r2);
		C_Thread rt3 = new C_Thread(r3);
		C_Thread rt4 = new C_Thread(r4);
		C_Thread rt5 = new C_Thread(r5);
		pt.start();
		rt1.start();
		rt2.start();
		rt3.start();
		rt4.start();
		rt5.start();
	}

}
