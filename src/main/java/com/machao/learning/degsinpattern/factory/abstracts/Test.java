package com.machao.learning.degsinpattern.factory.abstracts;

public class Test {
	public static void main(String[] args) {
		CarFactory bmw = new BMWFactory();
		System.out.println("BMW生产汽车");
		bmw.creatEngine().run();
		bmw.creatGearbox().run();
		System.out.println();
		CarFactory byd = new BYDFactory();
		System.out.println("BYD生产汽车");
		byd.creatEngine().run();
		byd.creatGearbox().run();
	}
}
