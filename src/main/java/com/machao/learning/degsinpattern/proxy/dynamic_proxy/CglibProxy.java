package com.machao.learning.degsinpattern.proxy.dynamic_proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CglibProxy implements MethodInterceptor {

	private Object target;

	public Object getInstance(Object target) {
		this.target = target;

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(target.getClass());
		enhancer.setCallback(this);
		return enhancer.create();
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		System.out.println("方法之前！");
		Object invoke = method.invoke(target, args);
		System.out.println("方法之后！");
		return invoke;
	}

	public static void main(String[] args) {
		CglibProxy proxy = new CglibProxy();
		InstanceImpl instance = (InstanceImpl) proxy.getInstance(new InstanceImpl());
		instance.method1("cglib方法1");
		System.out.println();
		instance.method2("cglib方法2");
	}

}
