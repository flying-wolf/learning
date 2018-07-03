package com.machao.learning.thread.JavaMultiThread.ChapterThree.InheritableThreadLocal1;

import java.util.Date;

public class InheritableThreadLocalExt extends InheritableThreadLocal<Object> {
	@Override
	protected Object initialValue() {
		// TODO Auto-generated method stub
		return new Date().getTime();
	}
}
