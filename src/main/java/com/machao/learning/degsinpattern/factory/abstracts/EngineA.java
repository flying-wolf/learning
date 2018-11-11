package com.machao.learning.degsinpattern.factory.abstracts;

public class EngineA implements Engine {

	@Override
	public void run() {
		System.out.println("使用V8发动机，速度快！");
	}

}
