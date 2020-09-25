package com.pig4cloud.plugin.idempotent.expression;

import com.pig4cloud.plugin.idempotent.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;

/**
 * @author lengleng
 * @date 2020/9/25
 * <p>
 * 唯一标志处理器
 */
public interface KeyResolver {

	/**
	 * 解析处理 key
	 * @param idempotent 接口注解标识
	 * @param point 接口切点信息
	 * @return 处理结果
	 */
	String resolver(Idempotent idempotent, JoinPoint point);

}
