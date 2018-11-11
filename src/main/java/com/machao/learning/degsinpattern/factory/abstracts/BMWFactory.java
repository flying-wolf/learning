package com.machao.learning.degsinpattern.factory.abstracts;

public class BMWFactory implements CarFactory {

	@Override
	public Engine creatEngine() {
		return new EngineA();
	}

	@Override
	public Gearbox creatGearbox() {
		return new GearboxA();
	}

}
