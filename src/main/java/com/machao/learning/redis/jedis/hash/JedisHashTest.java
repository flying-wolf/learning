package com.machao.learning.redis.jedis.hash;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

public class JedisHashTest {

	public static void main(String[] args) {
		Jedis jedis = null;
		try {
			System.out.println("=======集合（Set）=======");
			jedis = new Jedis("localhost", 6379);
			jedis.select(3);
			jedis.flushDB();
			Map<String, String> map = new HashMap<String, String>();
			map.put("key001", "value001");
			map.put("key002", "value002");
			map.put("key003", "value003");
			jedis.hmset("hash", map);
			jedis.hset("hash", "key004", "value004");
			// return Map<String,String>
			System.out.println("散列hash的所有键值对为：" + jedis.hgetAll("hash"));
			// return Set<String>
			System.out.println("散列hash的所有键为：" + jedis.hkeys("hash"));
			// return List<String>
			System.out.println("散列hash的所有值为：" + jedis.hvals("hash"));
			System.out.println("将key006保存的值加上一个整数，如果key006不存在则添加key006：" + jedis.hincrBy("hash", "key006", 6));
			System.out.println("散列hash的所有键值对为：" + jedis.hgetAll("hash"));
			System.out.println("将key006保存的值加上一个整数，如果key006不存在则添加key006：" + jedis.hincrBy("hash", "key006", 3));
			System.out.println("散列hash的所有键值对为：" + jedis.hgetAll("hash"));
			System.out.println("删除一个或者多个键值对：" + jedis.hdel("hash", "key002"));
			System.out.println("散列hash的所有键值对为：" + jedis.hgetAll("hash"));
			System.out.println("散列hash中键值对的个数：" + jedis.hlen("hash"));
			System.out.println("判断hash中是否存在key002：" + jedis.hexists("hash", "key002"));
			System.out.println("判断hash中是否存在key003：" + jedis.hexists("hash", "key003"));
			System.out.println("获取hash中的值：" + jedis.hmget("hash", "key003"));
			System.out.println("获取hash中的值：" + jedis.hmget("hash", "key003", "key004"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}

	}

}
