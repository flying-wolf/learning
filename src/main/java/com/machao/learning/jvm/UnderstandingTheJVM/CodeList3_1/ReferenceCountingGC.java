package com.machao.learning.jvm.UnderstandingTheJVM.CodeList3_1;

/**
 * testGC()方法执行后，objA和objB会不会被GC呢？
 * 
 * 在VM arguments中加入参数：
 * -XX:+PrintGCTimeStamps  	--打印GC时间信息
 * -XX:+PrintGCDetails		--打印GC详细信息
 * 
 * @author chao.ma
 */
public class ReferenceCountingGC {
	public Object instance = null;
	private static final int _1MB = 1024 * 1024;
	
	/**
	 * 这个成员属性的唯一意义就是占点内存，以便能在GC日志中看清楚是否被回收过
	 */
	private byte[] bigSize = new byte[2 * _1MB];
	
	public static void testGC() {
		ReferenceCountingGC objA = new ReferenceCountingGC();
		ReferenceCountingGC objB = new ReferenceCountingGC();
		objA.instance = objB;
		objB.instance = objA;
		objA = null;
		objB = null;
		// 假设在这行发生GC，ObjA和objB是否能被回收？
		System.gc();
	}
	
	public static void main(String[] args) {
		testGC();
	}
}
