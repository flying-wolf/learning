package com.machao.learning.degsinpattern.singleton.lazybones;

/**
 * 懒汉式
 * 
 * @author chao.ma
 */
public class Singleton {
	private static Singleton instance;

	private Singleton() {

	}

	public static synchronized Singleton newInstance() {
		if (instance == null) {
			instance = new Singleton();
		}
		return instance;
	}
}
