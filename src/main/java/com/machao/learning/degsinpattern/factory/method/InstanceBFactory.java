package com.machao.learning.degsinpattern.factory.method;

public class InstanceBFactory implements InstanceFactory {

	@Override
	public Instance create(String name) {
		return new InstanceB();
	}


}
