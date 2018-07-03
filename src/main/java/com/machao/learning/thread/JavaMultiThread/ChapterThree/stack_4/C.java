package com.machao.learning.thread.JavaMultiThread.ChapterThree.stack_4;

public class C {
	private MyStack stack;

	public C(MyStack stack) {
		super();
		this.stack = stack;
	}

	public void popService() {
		System.out.println("pop=" + stack.pop());
	}
}
