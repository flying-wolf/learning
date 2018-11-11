package com.machao.learning.degsinpattern.factory.abstracts;

public class GearboxA implements Gearbox {

	@Override
	public void run() {
		System.out.println("使用CVT变速箱！");
	}

}
