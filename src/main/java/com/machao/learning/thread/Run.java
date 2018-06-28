package com.machao.learning.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Run {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		for (int i = 0; i < 10; i++) {
			Test();
		}
	}

	private static void Test() throws InterruptedException, ExecutionException {
		CountDownLatch cdl = new CountDownLatch(6);
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 - 1);
		List<FutureTaskTest> taskList = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			FutureTaskTest ft = new FutureTaskTest(cdl);
			executorService.execute(ft);
			taskList.add(ft);
		}
		
		cdl.await(20 * 60L, TimeUnit.SECONDS);
		for (FutureTaskTest task : taskList) {
			if(task.isDone()) {
				System.out.println(task.get());
			} else {
				System.out.println("failed");
			}
		}
		executorService.shutdown();
	}

}
