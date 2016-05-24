package com.github.dumars.mybatis.pagination.dialect;

public interface Dialect {
	
	public String generateLimitSQL(String sql, int offset, int limit);
	
	public String generateCountSQL(String sql);
}

