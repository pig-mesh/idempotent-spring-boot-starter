package com.pig4cloud.plugin.idempotent.expression;

import com.pig4cloud.plugin.idempotent.controller.DemoController;
import org.junit.jupiter.api.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

class ExpressionResolverTest {

	private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

	@Test
	public void testParameterNameDiscoverer() throws Exception {
		String[] parameterNames = DISCOVERER
				.getParameterNames(DemoController.class.getDeclaredMethod("get", String.class));

		for (String parameterName : parameterNames) {
			System.out.println(parameterName);
		}
	}

}
