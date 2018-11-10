package com.machao.learning.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UserEntity {

	private static String staticStr;

	static {
		System.out.println("static...");
	}

	private UserEntity() {

	}

	private String userName;

	private void privateMethod() {
		System.out.println("the is private method!!!");
	}

	public void publicMethod() {
		System.out.println("the is public method!!!");
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchMethodException, SecurityException {
		// 第一种方式
		Class<?> c = Class.forName("com.machao.learning.reflection.UserEntity");

		for (Method m : c.getMethods()) {
			System.out.println("getMethods:" + m.getName());
		}

		for (Method m : c.getDeclaredMethods()) {
			System.out.println("getDeclaredMethods:" + m.getName());
		}

		for (Field f : c.getFields()) {
			System.out.println("getFields:" + f.getName());
		}

		for (Field f : c.getDeclaredFields()) {
			System.out.println("getDeclaredFields:" + f.getName());
		}

		UserEntity user = (UserEntity) c.newInstance();
		Constructor<?> cst = c.getDeclaredConstructor();
		cst.setAccessible(true);
		user.userName = "哈哈哈哈";
		System.out.println(user.userName);

		// 第2种
		Class<?> z = Class.forName("com.machao.learning.reflection.UserEntity");
		cst = z.getDeclaredConstructor();
		cst.setAccessible(true);

	}

}
