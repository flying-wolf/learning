package com.machao.learning.thread.JavaMultiThread.ChapterOne.t12;

public class Run2 {

	public static void main(String[] args) {
		Thread.currentThread().interrupt();
		System.out.println("是否已经停止1？："+Thread.interrupted());
		System.out.println("是否已经停止2？："+Thread.interrupted());
		System.out.println("end!");

	}

}
