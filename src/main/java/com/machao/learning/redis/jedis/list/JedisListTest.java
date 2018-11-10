package com.machao.learning.redis.jedis.list;

import redis.clients.jedis.Jedis;

public class JedisListTest {

	public static void main(String[] args) {
		Jedis jedis = null;

		try {
			jedis = new Jedis("localhost", 6379);

			// 选择数据库1
			jedis.select(1);
			jedis.flushDB();

			System.out.println("====列表list功能展示====");
			jedis.lpush("collections", "ArrayList", "LinkedList", "Vector", "Stack", "Queue");
			jedis.lpush("collections", "HashMap");
			jedis.lpush("collections", "HashMap");
			jedis.lpush("collections", "HashMap");
			jedis.lpush("number", "1");
			jedis.lpush("number", "2");
			jedis.lpush("number", "3");

			// -1代表倒数第一个
			System.out.println("collections 的内容：" + jedis.lrange("collections", 0, -1));
			System.out.println("collections区间0-2内容:" + jedis.lrange("collections", 0, 2));
			System.out.println("=================");
			// 删除列表指定的值 ，第二个参数为删除的个数（有重复时），后add进去的值先被删，类似于出栈
			System.out.println("删除指定元素个数：" + jedis.lrem("collections", 2, "HashMap"));
			System.out.println("collections 的内容:" + jedis.lrange("collections", 0, -1));
			System.out.println("删除区间0-4以外的数据:" + jedis.ltrim("collections", 0, 4));
			System.out.println("collections 的内容:" + jedis.lrange("collections", 0, -1));
			System.out.println("collections列表出栈（左端）：" + jedis.lpop("collections"));
			System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
			System.out.println("collections添加元素，从列表右端，与lpush相对应：" + jedis.rpush("collections", "EnumMap"));
			System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
			System.out.println("collections列表出栈（右端）：" + jedis.rpop("collections"));
			System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
			System.out.println("修改collections指定下标1的内容：" + jedis.lset("collections", 1, "LinkedArrayList"));
			System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
			System.out.println("=================");
			System.out.println("collections的长度：" + jedis.llen("collections"));
			System.out.println("获取collections下标为2的元素：" + jedis.lindex("collections", 2));
			System.out.println("=================");
			jedis.lpush("sortedList", "3", "6", "2", "0", "7", "4");
			System.out.println("sortedList排序前：" + jedis.lrange("sortedList", 0, -1));
			System.out.println(jedis.sort("sortedList"));
			System.out.println("sortedList排序后：" + jedis.lrange("sortedList", 0, -1));
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

}
