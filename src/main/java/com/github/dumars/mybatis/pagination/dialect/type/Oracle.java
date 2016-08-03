package com.github.dumars.mybatis.pagination.dialect.type;

import org.apache.commons.lang3.StringUtils;

import com.github.dumars.mybatis.pagination.dialect.Dialect;

public class Oracle implements Dialect {

	@Override
	public String generateLimitSQL(String sql, int offset, int limit) {
		sql = sql.trim();
		String forUpdate = null;
		int index = StringUtils.indexOf(sql, " for update");
		if(index > 0) {
			forUpdate = StringUtils.substring(sql, index);
			sql = StringUtils.substring(sql, 0, index);
		}
		
		StringBuilder sb = new StringBuilder(sql.length() + 100);
		if (offset > 0) {
			sb.append("select * from ( select row_.*, rownum rownum_ from ( ");
		} else {
			sb.append("select * from ( ");
		}
		
		sb.append(sql);
		
		if (offset > 0) {
			sb.append(" ) row_ ) where rownum_ <= ").append(offset + limit)
				.append(" and rownum_ > ").append(offset);
		} else {
			sb.append(" ) where rownum <= ").append(limit);
		}

		if (StringUtils.isNotEmpty(forUpdate)) {
			sb.append(forUpdate);
		}

		return sb.toString();
	}

	@Override
	public String generateCountSQL(String sql) {
		return "select count(1) from (" + sql + ")";
	}

}
