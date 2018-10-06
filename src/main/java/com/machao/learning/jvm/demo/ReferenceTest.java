package com.machao.learning.jvm.demo;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class ReferenceTest {

	public static void main(String[] args) {
		// StrongReference
		Object strongReferenceObj = new Object();
		
		//SoftReference
		SoftReference<Object> softReferenceObj = new SoftReference<Object>(new Object());
		
		//WeakReference
		WeakReference<Object> weakReferenceObj = new WeakReference<Object>(new Object());
		
		//PhantomReference
		PhantomReference<Object> phantomReferenceObj = new PhantomReference<Object>(new Object(), new ReferenceQueue<Object>());
		
		//WeakReference abc
		WeakReference<Object> abcObj = new WeakReference<Object>("abc");
		
		System.gc();
		
		System.out.println("StrongReference: "+strongReferenceObj);
		System.out.println("SoftReference: "+softReferenceObj.get());
		System.out.println("WeakReference: "+weakReferenceObj.get());
		System.out.println("PhantomReference: "+phantomReferenceObj.get());
		System.out.println("abc: "+abcObj.get());

	}

}
