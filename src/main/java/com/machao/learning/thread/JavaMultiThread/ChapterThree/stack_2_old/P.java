package com.machao.learning.thread.JavaMultiThread.ChapterThree.stack_2_old;

public class P {
	private MyStack stack;

	public P(MyStack stack) {
		super();
		this.stack = stack;
	}

	public void pushService() {
		stack.push();
	}
}
