package com.machao.learning.degsinpattern.factory.abstracts;

public class EngineB implements Engine {

	@Override
	public void run() {
		System.out.println("使用四缸发动机，速度慢！");
	}

}
