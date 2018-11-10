package com.machao.learning.redis.jedis.string;

import redis.clients.jedis.Jedis;

public class Demo {

	public static void main(String[] args) {
		Jedis jedis = new Jedis();
		jedis.set("aaaaaa", ":aaaaaa", "nx", "ex", 1000);
		jedis.close();

	}

}
