package com.machao.learning.reflection;

import java.lang.reflect.Constructor;

public class BasicUseDemo {
	
	/**
	 * 反射的基本运用
	 */
	public static void main(String[] args) throws Exception {
		/**
		 * ①.获得Class对象
		 * (1).使用Class类的forName()静态方法
		 * (2).直接获取对象的class
		 * (3).调用对象的getClass()方法
		 */
		
		//(1). 使用Class类的forName()静态方法
		Class<String> strClass1 = (Class<String>) Class.forName("java.lang.String");
		
		//(2). 直接获取对象的class
		Class<String> strClass2 = String.class;
		
		//(3). 调用对象实例的getClass()方法
		Class<String> strClass3 = (Class<String>) new String("123123").getClass();
		
		/**
		 * ②.判断是否为某个类的实例
		 * (1).使用Instanceof关键字判断
		 * (2).使用Class对象的isInstance()方法判断
		 */
		
		// (1).使用Instanceof关键字判断
		if(strClass1 instanceof Class) {
			System.out.println("strClass1 是 Class 的实例！");
		}
		// （2).调用Class对象的isInstance(obj)方法判断obj是否可以转化为Class的对象。
		if(strClass1.isInstance(new String())) {
			System.out.println("new String() 可以转化为 strClass1 的对象！");
		}
		
		/**
		 * ③.创建实例
		 * (1).使用Class对象的newInstance()方法
		 * (2).先通过Class对象的getConstructor()方法获取Constructor对象，在调用Constructor对象的newInstance()方法创建实例
		 */
		
		// (1).使用Class对象的newInstance()方法
		Class<String> strClass = String.class;
		String str = strClass.newInstance();
		System.out.println(str.hashCode());
		
		// (2).先通过Class对象的getConstructor()方法获取Constructor对象，在调用Constructor对象的newInstance()方法创建实例
		Constructor<String> constructor = strClass1.getDeclaredConstructor(strClass1);
		String string = constructor.newInstance("aaaaaffffffff");
		System.out.println(string);
		
	}

}
