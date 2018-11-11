package com.machao.learning.degsinpattern.factory.abstracts;

public interface CarFactory {
	Engine creatEngine();

	Gearbox creatGearbox();
}
