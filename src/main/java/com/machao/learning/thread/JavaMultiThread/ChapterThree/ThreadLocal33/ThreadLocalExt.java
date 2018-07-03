package com.machao.learning.thread.JavaMultiThread.ChapterThree.ThreadLocal33;

import java.util.Date;

public class ThreadLocalExt extends ThreadLocal<Object> {
	@Override
	protected Object initialValue() {
		return new Date().getTime();
	}
}
