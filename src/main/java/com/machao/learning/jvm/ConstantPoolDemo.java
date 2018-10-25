package com.machao.learning.jvm;

/**
 * 常量池等相关问题
 * 
 * @author chao.ma
 *
 *         intern函数的作用是将对应的符号常量进入特殊处理，在1.6以前 和 1.7以后有不同的处理；
 * 
 *         1.6：
 * 
 *         在1.6中，intern的处理是
 *         先判断字符串常量是否在字符串常量池中，如果存在直接返回该常量，如果没有找到，则将该字符串常量加入到字符串常量区，也就是在字符串常量区建立该常量；
 * 
 *         1.7：
 * 
 *         在1.7中，intern的处理是
 *         先判断字符串常量是否在字符串常量池中，如果存在直接返回该常量，如果没有找到，说明该字符串常量在堆中，则处理是把堆区该对象的引用加入到字符串常量池中，以后别人拿到的是该字符串常量的引用，实际存在堆中；【这里感谢以为网友的纠正，一开始理解为在堆区建立该字符串对象在添加引用了，其实调用该方法的字符串对象要么在堆区要么在常量池中的】
 */
public class ConstantPoolDemo {
	public static void main(String[] args) {
		MethodOne();
		MethodTwo();
		MethodThree();
		MethodFour();
	}

	private static void MethodFour() {
		// 常量池
		Integer a = 1;
		// 常量池
		Integer b = 2;
		// 常量池
		Integer c = 3;
		// 常量池
		Integer d = 3;
		// int常量池中只处理-128~127的数值，321已超出范围，所以存在堆中，e指向堆
		Integer e = 321;
		// int常量池中只处理-128~127的数值，321已超出范围，所以存在堆中，f指向堆
		Integer f = 321;

		Long g = 3L;

		// == 两边没有算数运算时比较地址，c和d都指向常量池，所以为true
		System.out.println(c == d);
		// e和f分别是两个不同的堆对象，地址比较false
		System.out.println(e == f);
		// == 此时==两边有算术运算，会进行拆箱，因此此时比较的是数值，而并非对象，true
		System.out.println(c == (a + b));
		// c与a+b的数值相等，为true。
		System.out.println(c.equals(a + b));
		// a+b 自动拆箱运算后与g比较数值相等，true
		System.out.println(g == (a + b));
		// equals不处理数据类型的关系，a+b计算后不是long类型，所以false
		System.out.println(g.equals(a + b));
	}

	private static void MethodThree() {
		// 在常量池创建"2",然后在堆new一个对象"2",t1指向堆
		String t1 = new String("2");
		// 常量池中已经存在"2"，t2指向常量池
		String t2 = "2";
		// 此时常量池已经存在"2"，直接返回常量池"2"
		t1.intern();
		// t1指向的堆，t2指向常量池，所以false
		System.out.println(t1 == t2); // false or true?

		// StringBuffer拼接"2"和"2",在堆中new一个"22"，t3指向堆中的“22”
		String t3 = new String("2") + new String("2");
		// 此时常量池没有"22" ,在常量池创建"22",t4指向常量池中的"22"
		String t4 = "22";
		// 此时常量池已经有"22"，返回常量池的"22"
		t3.intern();
		// t3指向的是堆,t4指向了常量池，地址不同，false
		System.out.println(t3 == t4); // false or true?
	}

	private static void MethodTwo() {
		// 在常量池创建"aaa"，str1指向常量池
		String str1 = "aaa";
		// 在常量池创建"bbb"，str2指向常量池
		String str2 = "bbb";
		// 在常量池创建"aaabbb"，str3指向常量池
		String str3 = "aaabbb";
		// StringBuffer的append拼接字符串，toString()new一个"aaabbb"在堆，str4指向堆
		String str4 = str1 + str2;
		// jvm优化处理，在编译阶段将"aaa"和"bbb"进行拼接放入常量池，str5指向常量池
		String str5 = "aaa" + "bbb";
		// str3指向常量池，str4指向的是堆，所以地址不同，为false
		System.out.println(str3 == str4); // false or true？
		// str3指向常量池，str4.intern()方法发现常量池已经有"aaabbb"则返回常量池中的对象，所以为true
		System.out.println(str3 == str4.intern()); // true or false？
		// str3和str5都指向常量池中的"aaabbb"，所以为true
		System.out.println(str3 == str5);// true or false？
	}

	private static void MethodOne() {
		// 1.在常量池中创建"1"的对象
		// 2.在堆中new一个"1"的对象，str1指向堆
		String str1 = new String("1");
		// 查看常量池是否已经存在"1"的对象，如果存在直接返回常量池，如果不存在
		// JDK1.6会在常量池创建"1"的对象，JDK1.7会将“1”在堆中的引用放入常量池
		str1.intern();
		// 此时常量池已存在"1"，str2指向常量池
		String str2 = "1";
		// str1指向堆，str2指向常量池，所以false
		System.out.println(str1 == str2); // 结果是 false or true？

		// 底层实现会用StringBuffer的append方法将两个"2"拼接，然后toString()方法在堆中new一个对象"22",str3指向堆
		String str3 = new String("2") + new String("2");
		// 此时常量池没有此对象，则1.6中会在常量池创建"22"的对象，1.7中会将堆中的引用放入常量池
		str3.intern();
		// 此时常量池中已有"22"，则str4指向常量池
		String str4 = "22";
		// 1.6位false,1.7为true
		System.out.println(str3 == str4); // 结果是 false or true？
	}
}
