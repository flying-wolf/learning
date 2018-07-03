package com.machao.learning.thread.JavaMultiThread.ChapterThree.InheritableThreadLocal2;

import java.util.Date;

public class InheritableThreadLocalExt extends InheritableThreadLocal<Object> {
	@Override
	protected Object initialValue() {
		// TODO Auto-generated method stub
		return new Date().getTime();
	}
	@Override
	protected Object childValue(Object parentValue) {
		// TODO Auto-generated method stub
		return parentValue + " 我在子线程加的～！";
	}
}
