package com.machao.learning.degsinpattern.singleton.hungry;

/**
 * 饿汉式
 * 
 * @author chao.ma
 *
 */
public class Singleton {

	private static Singleton instance = new Singleton();

	private Singleton() {

	}

	public static Singleton newInstance() {
		return instance;
	}
}
