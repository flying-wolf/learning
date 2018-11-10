package com.machao.learning.redis.jedis.string;

import redis.clients.jedis.Jedis;

public class JedisStringTest {

	public static void main(String[] args) throws InterruptedException {
		// 建立连接
		Jedis jedis = new Jedis("localhost", 6379);
		// 选择数据库
		jedis.select(0);
		// 清空当前数据库
		jedis.flushDB();

		// 添加String类型缓存
		jedis.set("name", "chao.ma");
		// 获取缓存
		System.out.println("name的值为：" + jedis.get("name"));
		// 添加缓存，设置失效时间
		// nxxx:nx 紧当key不存在时，xx 紧当key存在时
		// expx: ex 秒， px 毫秒妙
		jedis.set("flag", "success", "nx", "ex", 10);

		// 暂停4秒后查看过期时间
		Thread.sleep(4000);
		System.out.println("flag的过期时间为：" + jedis.ttl("flag"));

		// 暂停8秒后查看过期的缓存是否存在
		Thread.sleep(8000);
		System.out.println("flag是否存在：" + jedis.exists("flag"));

		// 批量插入数据
		jedis.mset("a", "1.0.0", "b", "2.0.0");
		// 批量查询
		System.out.println("a和b的值为：" + jedis.mget("a", "b"));

		// 累加、累减
		jedis.set("count", "1");
		System.out.println(String.format("当前在线人数%s", jedis.get("count")));
		System.out.println(String.format("又上线一个用户后，当前在线人数为%s", jedis.incr("count")));
		System.out.println(String.format("此时有18人上线，当前在线人数为%s", jedis.incrBy("count", 18)));
		System.out.println(String.format("下线一个用户后，当前在线人数为%s", jedis.decr("count")));
		System.out.println(String.format("此时有6人下线，当前在线人数为%s", jedis.decrBy("count", 6)));

		// 截取字符串
		jedis.set("email", "sggw@163.com.cn");
		System.out.println("email:" + jedis.get("email"));
		System.out.println("修改后的email:" + jedis.substr("email", 0, 11));

		// 拼接字符串
		jedis.append("email", ".org");
		System.out.println("email:" + jedis.get("email"));

		// 删除a和b
		jedis.del("a", "b");
		
		// 关闭连接
		jedis.close();
	}

}
