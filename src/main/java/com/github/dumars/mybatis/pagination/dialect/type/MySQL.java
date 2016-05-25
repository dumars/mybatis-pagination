package com.github.dumars.mybatis.pagination.dialect.type;

import com.github.dumars.mybatis.pagination.dialect.Dialect;

public class MySQL implements Dialect {

	@Override
	public String generateLimitSQL(String sql, int offset, int limit) {
		return sql + " limit " + offset + ", " + limit;
	}

	@Override
	public String generateCountSQL(String sql) {
		return "select count(1) from (" + sql + ") as my_count";
	}

}
