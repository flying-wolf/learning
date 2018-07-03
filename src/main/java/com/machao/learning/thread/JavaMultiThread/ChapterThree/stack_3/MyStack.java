package com.machao.learning.thread.JavaMultiThread.ChapterThree.stack_3;

import java.util.ArrayList;
import java.util.List;

public class MyStack {
	private List<String> list = new ArrayList<>();

	public synchronized void push() {
		try {
			while (list.size() == 1) {
				this.wait();
			}
			list.add("anyString="+Math.random());
			this.notifyAll();
			System.out.println("push=" + list.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized String pop() {
		String returnValue = "";
		try {
			while (list.size() == 0) {
				this.wait();
				System.out.println("pop操作中的" + Thread.currentThread().getName() + "线程呈wait状态");
			}
			returnValue = list.get(0);
			list.remove(0);
			this.notifyAll();
			System.out.println("pop=" + list.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return returnValue;
	}
}
