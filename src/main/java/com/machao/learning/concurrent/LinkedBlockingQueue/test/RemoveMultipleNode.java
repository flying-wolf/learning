package com.machao.learning.concurrent.LinkedBlockingQueue.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoveMultipleNode {

	public static void main(String[] args) {
		BlockingQueue<String> bq = new LinkedBlockingQueue<>();
		bq.add("123");
		bq.add("456");
		bq.add("1244");
		bq.add("1244");
		bq.add("321");

		bq.remove("1244");
		List<String> list = new ArrayList<>();
		bq.drainTo(list);
		System.out.println(list.toString());

	}

}
