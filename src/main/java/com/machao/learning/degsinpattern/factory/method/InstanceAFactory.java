package com.machao.learning.degsinpattern.factory.method;

public class InstanceAFactory implements InstanceFactory {

	@Override
	public Instance create(String name) {
		return new InstanceA();
	}


}
