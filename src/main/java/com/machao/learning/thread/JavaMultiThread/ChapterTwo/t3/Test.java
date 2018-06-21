package com.machao.learning.thread.JavaMultiThread.ChapterTwo.t3;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			PublicVar publicVarRef = new PublicVar();
			ThreadA thread = new ThreadA(publicVarRef);
			thread.start();
			Thread.sleep(200);//打印结果受此值大小影响
			publicVarRef.getValue();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
