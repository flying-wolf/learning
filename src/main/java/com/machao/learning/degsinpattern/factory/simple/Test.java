package com.machao.learning.degsinpattern.factory.simple;

public class Test {

	public static void main(String[] args) {
		InstanceFactory factory = new InstanceFactory();
		factory.create("a").run();
		factory.create("b").run();
	}

}
