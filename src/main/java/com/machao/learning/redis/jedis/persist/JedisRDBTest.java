package com.machao.learning.redis.jedis.persist;

import redis.clients.jedis.Jedis;

public class JedisRDBTest {
	public static void main(String[] args) {
		Jedis jedis = null;
		try {
			jedis = new Jedis();
			jedis.select(8);
			jedis.flushDB();
			for (int i = 1; i <= 1000; i++) {
				jedis.set("test-"+i, String.valueOf(i));
			}
			
			System.out.println(jedis.lastsave());
			jedis.save();
			//jedis.bgsave();
			System.out.println(jedis.lastsave());
			jedis.close();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis != null)
				jedis.close();
		}
	}
}
