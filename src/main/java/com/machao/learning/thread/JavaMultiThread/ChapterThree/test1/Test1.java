package com.machao.learning.thread.JavaMultiThread.ChapterThree.test1;

public class Test1 {

	public static void main(String[] args) {
		try {
			String newString = new String();
			newString.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
