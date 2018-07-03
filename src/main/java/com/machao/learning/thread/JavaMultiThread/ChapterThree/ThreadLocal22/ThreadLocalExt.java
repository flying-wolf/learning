package com.machao.learning.thread.JavaMultiThread.ChapterThree.ThreadLocal22;

public class ThreadLocalExt extends ThreadLocal<String> {
	@Override
	protected String initialValue() {
		return "我的默认值 第一次get不在为null";
	}
}
