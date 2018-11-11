package com.machao.learning.degsinpattern.proxy.static_proxy;

public class InstanceProxy implements Instance {

	private Instance instance;
	
	public InstanceProxy(Instance instance) {
		super();
		this.instance = instance;
	}

	@Override
	public void method1(String field) {
		System.out.println("start method1()!");
		instance.method1(field);
		System.out.println("end method1()!");
	}

	@Override
	public void method2(String field) {
		System.out.println("start method2()!");
		instance.method1(field);
		System.out.println("end method2()!");
	}

}
