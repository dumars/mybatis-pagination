package com.github.dumars.mybatis.pagination.utils;

import java.util.Map;

import org.junit.Test;
import org.springframework.util.Assert;

import com.github.dumars.mybatis.pagination.dialect.Dialect;

public class MybatisUtilsTest {

	@Test
	public void test() {
		Map<String, Class<? extends Dialect>> dialects = MybatisUtils.getSupportDatabaseType();
		Assert.notEmpty(dialects);
		Assert.isTrue(dialects.containsKey("MYSQL"));
	}
}
