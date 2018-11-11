package com.machao.learning.degsinpattern.factory.simple;

import org.springframework.util.StringUtils;

public class InstanceFactory {
	public Instance create(String name) {
		if (StringUtils.isEmpty(name))
			return null;
		if (name.equalsIgnoreCase("a"))
			return new InstanceA();
		if (name.equalsIgnoreCase("b"))
			return new InstanceB();
		return null;
	}
}
