package com.machao.learning.degsinpattern.proxy.static_proxy;

public class Test {

	public static void main(String[] args) {
		Instance instance = new InstanceProxy(new InstanceImpl());
		instance.method1("方法1");
		instance.method2("方法2");
	}

}
