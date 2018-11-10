package com.machao.learning.redis.jedis.set;

import redis.clients.jedis.Jedis;

public class JedisSetTest {

	public static void main(String[] args) {
		Jedis jedis = null;
		try {
			jedis = new Jedis("localhost", 6379);
			jedis.select(2);
			jedis.flushDB();

			System.out.println("========测试集合（set）=========");
			System.out.println("集合set添加数据：" + jedis.sadd("setElement", "e1", "e7", "e3", "e6", "e0", "e4"));
			System.out.println(jedis.sadd("setElement", "e6"));
			System.out.println("setElement的所有元素：" + jedis.smembers("setElement"));
			System.out.println("删除元素e0:" + jedis.srem("setElement", "e0"));
			System.out.println("setElement的所有元素：" + jedis.smembers("setElement"));
			System.out.println("删除两个元素e7和e6：" + jedis.srem("setElement", "e7", "e6"));
			System.out.println("setElement的所有元素为：" + jedis.smembers("setElement"));
			System.out.println("随机的移除集合中的一个元素：" + jedis.spop("setElement"));
			System.out.println("随机的移除集合中的一个元素：" + jedis.spop("setElement"));
			System.out.println("setElement的所有元素为：" + jedis.smembers("setElement"));
			System.out.println("setElement中包含元素的个数：" + jedis.scard("setElement"));
			System.out.println("e3是否在setElement中：" + jedis.sismember("setElement", "e3"));
			System.out.println("e1是否在setElement中：" + jedis.sismember("setElement", "e1"));

			System.out.println("=================");
			System.out.println(jedis.sadd("setElement1", "e1", "e2", "e4", "e3", "e0", "e8", "e7", "e5"));
			System.out.println(jedis.sadd("setElement2", "e1", "e2", "e4", "e3", "e0", "e8"));
			System.out.println("将setElement1中删除e1并存入setElement3中：" + jedis.smove("setElement1", "setElement3", "e1"));
			System.out.println("将setElement1中删除e2并存入setElement3中：" + jedis.smove("setElement1", "setElement3", "e2"));
			System.out.println("setElement1中的元素：" + jedis.smembers("setElement1"));
			System.out.println("setElement3中的元素：" + jedis.smembers("setElement3"));

			System.out.println("集合运算:");
			System.out.println("setElement1中的元素：" + jedis.smembers("setElement1"));
			System.out.println("setElement2中的元素：" + jedis.smembers("setElement2"));
			System.out.println("setElement1和setElement2的交集:" + jedis.sinter("setElement1", "setElement2"));
			System.out.println("setElement1和setElement2的并集:" + jedis.sunion("setElement1", "setElement2"));
			// setElement1中有，setElement2中没有
			System.out.println("setElement1和setElement2的差集:" + jedis.sdiff("setElement1", "setElement2"));
			System.out.println();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}

	}

}
