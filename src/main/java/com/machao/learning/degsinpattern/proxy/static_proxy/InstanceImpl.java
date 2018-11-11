package com.machao.learning.degsinpattern.proxy.static_proxy;

public class InstanceImpl implements Instance {

	@Override
	public void method1(String field) {
		System.out.println("run method1(), field:"+field);
	}

	@Override
	public void method2(String field) {
		System.out.println("run method2(), field:"+field);
	}

}
