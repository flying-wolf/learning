package com.machao.learning.degsinpattern.factory.method;

public class Test {

	public static void main(String[] args) {
		InstanceFactory factoryA = new InstanceAFactory();
		InstanceFactory factoryB = new InstanceBFactory();
		factoryA.create("a").run();
		factoryB.create("b").run();
	}

}
