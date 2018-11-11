package com.machao.learning.degsinpattern.factory.abstracts;

public class BYDFactory implements CarFactory {

	@Override
	public Engine creatEngine() {
		return new EngineB();
	}

	@Override
	public Gearbox creatGearbox() {
		return new GearboxB();
	}

}
