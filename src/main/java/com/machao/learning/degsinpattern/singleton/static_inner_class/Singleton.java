package com.machao.learning.degsinpattern.singleton.static_inner_class;

/**
 * 静态内部类方式
 * 
 * @author chao.ma
 *
 */
public class Singleton {
	private static class SingletonHolder {
		private static Singleton instance = new Singleton();
	}

	private Singleton() {

	}

	public static Singleton newInstance() {
		return SingletonHolder.instance;
	}
}
