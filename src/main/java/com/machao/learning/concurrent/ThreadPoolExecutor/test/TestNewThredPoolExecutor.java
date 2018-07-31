package com.machao.learning.concurrent.ThreadPoolExecutor.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestNewThredPoolExecutor {
	public static void main(String[] args) {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(5));
		for (int i = 1; i <= 15; i++) {
			MyTask myTask = new MyTask(i);
			executor.execute(myTask);
			System.out.println(String.format("线程池中线程数目：%d，队列中等待执行的任务数目：%d，已执行完毕的任务数目：%d 。", executor.getPoolSize(),
					executor.getQueue().size(), executor.getCompletedTaskCount()));
		}
		executor.shutdown();
	}

}

class MyTask implements Runnable {
	private int taskNum;

	public MyTask(int num) {
		this.taskNum = num;
	}

	@Override
	public void run() {
		System.out.println("正在执行task" + taskNum);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("task " + taskNum + "执行完毕");
	}
}