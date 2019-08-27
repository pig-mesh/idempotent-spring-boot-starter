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

@RunWith(SpringRunner.class)
@SpringBootTest
public class IdempotentApplicationTests {

	@Autowired
	private Redisson redisson;

	@Test
	public void contextLoads() {
		RMapCache<Object, Object> rMap = redisson.getMapCache("idempotent");
		String key = "localhost:1999?name=wang";
		Object t = rMap.putIfAbsent(key, LocalDateTime.now().toString().replace("T", " "), 10,
				TimeUnit.SECONDS);


		Object o = rMap.get(key);
		System.out.println(o.toString());

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
