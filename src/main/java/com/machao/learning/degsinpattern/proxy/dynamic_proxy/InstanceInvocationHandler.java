package com.machao.learning.degsinpattern.proxy.dynamic_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class InstanceInvocationHandler implements InvocationHandler {

	private Object target;

	public InstanceInvocationHandler(Object target) {
		super();
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("方法执行之前！");
		Object invoke = method.invoke(target, args);
		System.out.println("方法执行之后！");
		return invoke;
	}

	public static void main(String[] args) {
		InstanceInvocationHandler handler = new InstanceInvocationHandler(new InstanceImpl());
		// 获取类加载器
		ClassLoader loader = InstanceImpl.class.getClassLoader();
		// 获取接口
		Class<?>[] interfaces = InstanceImpl.class.getInterfaces();
		// new一个代理实例
		Instance instance = (Instance) Proxy.newProxyInstance(loader, interfaces, handler);
		instance.method1("方法1");
		System.out.println("-----------------------------");
		instance.method2("方法2");

	}
}
