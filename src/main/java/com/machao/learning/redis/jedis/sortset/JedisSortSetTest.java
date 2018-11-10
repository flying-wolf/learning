package com.machao.learning.redis.jedis.sortset;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

public class JedisSortSetTest {

	public static void main(String[] args) {
		Jedis jedis = null;
		try {
			jedis = new Jedis("localhost", 6379);
			jedis.select(4);
			jedis.flushDB();
			
			System.out.println("=======有序集合=======");
	        Map<String, Double> map = new HashMap<String, Double>();
	        map.put("key2", 1.2);
	        map.put("key3", 4.0);
	        map.put("key4", 5.0);
	        map.put("key5", 0.2);
	        System.out.println(jedis.zadd("zset", 3, "key1"));
	        System.out.println(jedis.zadd("zset", map));
	        System.out.println("zset中的所有元素：" + jedis.zrange("zset", 0, -1));
	        System.out.println("zset中的所有元素：" + jedis.zrangeWithScores("zset", 0, 3));
	        System.out.println("zset中的所有元素：" + jedis.zrangeByScore("zset", 0, 100));
	        System.out.println("zset中的所有元素：" + jedis.zrangeByScoreWithScores("zset", 0, 100));
	        System.out.println("zset中key2的分值：" + jedis.zscore("zset", "key2"));
	        System.out.println("zset中key2的排名：" + jedis.zrank("zset", "key2"));
	        System.out.println("删除zset中的元素key3：" + jedis.zrem("zset", "key3"));
	        System.out.println("zset中的所有元素：" + jedis.zrange("zset", 0, -1));
	        System.out.println("zset中元素的个数：" + jedis.zcard("zset"));
	        System.out.println("zset中分值在1-4之间的元素的个数：" + jedis.zcount("zset", 1, 4));
	        System.out.println("key2的分值加上5：" + jedis.zincrby("zset", 5, "key2"));
	        System.out.println("key3的分值加上4：" + jedis.zincrby("zset", 4, "key3"));
	        System.out.println("zset中的所有元素：" + jedis.zrange("zset", 0, -1));
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis != null)
				jedis.close();
		}

	}

}
