package com.machao.learning.collection.HashMap;

public class Main {

	public static void main(String[] args) {
		System.out.println(16 >>> 1);
		System.out.println((16 - (16 >>> 2)));
		
		/*System.out.println(2 >> 17);
		int MAXIMUM_CAPACITY = 1 << 30;
		int cap = 12;
		int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        int result = (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        System.out.println(result);
        System.out.println(cap << 1);
        System.out.println(15 & 123);*/
		
		int MAXIMUM_CAPACITY = 1 << 30;
		int c = 12 + (12 >>> 1) + 1;
		System.out.println("c="+c);
		
		int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        int result = (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        System.out.println(result);
        System.out.println(3 << 1);
        
        System.out.println(16 - (16 >>> 2));
        
        System.out.println((16 << 1) - (16 >>> 1));
		
		
	}

}
