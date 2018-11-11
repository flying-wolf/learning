package com.machao.learning.degsinpattern.singleton.double_check_lock;

/**
 * 双重检验锁
 * 
 * @author chao.ma
 */
public class Singleton {
	private static volatile Singleton instance;

	private Singleton() {

	}

	public static Singleton newInstance() {
		if (instance == null) {
			synchronized (Singleton.class) {
				if (instance == null) {
					instance = new Singleton();
				}
			}
		}
		return instance;
	}
}
