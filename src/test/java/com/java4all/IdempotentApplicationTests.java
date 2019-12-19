package com.java4all;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * putIfAbsent方法：
 （1）如果是新的记录，那么会向map中添加该键值对，并返回null。
 （2）如果已经存在，那么不会覆盖已有的值，直接返回已经存在的值。
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class IdempotentApplicationTests {

	@Autowired
	private Redisson redisson;

	@Test
	public void testNullMap() {
		RMapCache<Object, Object> aaa = redisson
				.getMapCache("adsafasffsdaxdsdfsdffsdfsdffsadfafsaf");
		aaa.putIfAbsent("wang",123);
		System.out.println("aaaaa");
	}

	@Test
	public void testPutIfAbsent() {
		RMapCache<Object, Object> rMap = redisson.getMapCache("idempotent");
		String key = "localhost:1999?name=wang";
		String tt1 = LocalDateTime.now().toString().replace("T", " ");
		Object t1 = rMap.putIfAbsent(key,tt1 , 10,
				TimeUnit.SECONDS);
		System.out.println("tt1="+tt1);
		System.out.println("t1="+t1);

		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String tt2 = LocalDateTime.now().toString().replace("T", " ");
		Object t2 = rMap.putIfAbsent(key,tt2 , 10,
				TimeUnit.SECONDS);
		System.out.println("tt2="+tt2);
		System.out.println("t2="+t2);


		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String tt3 = LocalDateTime.now().toString().replace("T", " ");
		Object t3 = rMap.putIfAbsent(key,tt3 , 10,
				TimeUnit.SECONDS);
		System.out.println("tt3="+tt3);
		System.out.println("t3="+t3);


		Object o = rMap.get(key);
		System.out.println("get by key:"+o.toString());

		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("休眠结束");
		Object o1 = rMap.get(key);
		System.out.println(o1.toString());
	}

}
