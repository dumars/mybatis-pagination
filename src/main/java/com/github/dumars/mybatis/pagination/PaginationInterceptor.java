package com.github.dumars.mybatis.pagination;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.github.dumars.mybatis.pagination.dialect.Dialect;
import com.github.dumars.mybatis.pagination.dialect.type.MySQL;
import com.github.dumars.mybatis.pagination.entity.Page;
import com.github.dumars.mybatis.pagination.entity.PageContext;
import com.github.dumars.mybatis.pagination.utils.MybatisUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class,
		Object.class, RowBounds.class, ResultHandler.class }) })
public class PaginationInterceptor implements Interceptor {

	private final static String SQL_PATTERN = ".*WithPagination.*";
	private Dialect dialect;
	private String nameRegularExpress;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		intercept(invocation.getArgs());
		return invocation.proceed();
	}

	private void intercept(final Object[] args) {
		//args: MappedStatement, Object, RowBounds, ResultHandler
		MappedStatement ms = (MappedStatement) args[0];
		Object parameter = args[1];

		if (ms.getId().matches(nameRegularExpress)) {
			BoundSql boundSql = ms.getBoundSql(parameter);
			Page page = PageContext.get();
			String sql = dialect.generateLimitSQL(boundSql.getSql().trim(), page.getOffset(),
					page.getPageSize());

			args[2] = RowBounds.DEFAULT;

			// Count total rows
			try {
				Connection conn = ms.getConfiguration().getEnvironment().getDataSource().getConnection();
				int count = MybatisUtils.count(boundSql.getSql().trim(), conn, ms, parameter, boundSql, dialect);

				page.setTotalRows(count);
				PageContext.set(page);

				if (log.isDebugEnabled()) {
					log.debug(ToStringBuilder.reflectionToString(page, ToStringStyle.SHORT_PREFIX_STYLE));
				}
			} catch (SQLException e) {
				log.error("Pagination count fail.", e);
			}
			
			BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(),
					boundSql.getParameterObject());

			MappedStatement state = changeSqlSource(ms, new BoundSqlSqlSource(newBoundSql));

			boundSql.getParameterMappings().stream()
					.filter(filter -> filter.getProperty()
					.startsWith(ForEachSqlNode.ITEM_PREFIX))
					.forEach(mapping -> {
						String propertyName = mapping.getProperty();
						newBoundSql.setAdditionalParameter(propertyName, boundSql.getAdditionalParameter(propertyName));
					});

			args[0] = state;
		}
	}

	private MappedStatement changeSqlSource(MappedStatement ms, SqlSource sqlSource) {
		Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), sqlSource,
				ms.getSqlCommandType());

		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		String[] keyProperties = ms.getKeyProperties();
		builder.keyProperty(keyProperties == null ? null : keyProperties[0]);
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		builder.resultMaps(ms.getResultMaps());
		builder.resultSetType(ms.getResultSetType());
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());

		return builder.build();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		nameRegularExpress = properties.getProperty("nameRegularExpress");
		if(StringUtils.isEmpty(nameRegularExpress)) {
			nameRegularExpress = SQL_PATTERN;
		}
		
		String databaseType = properties.getProperty("databaseType");
		Map<String, Class<? extends Dialect>> map = MybatisUtils.getSupportDatabaseType();
		if(StringUtils.isNotEmpty(databaseType) && map.containsKey(databaseType)) {
			try {
				dialect = map.get(databaseType).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				log.error("Create new dialect type error.", e);
			}
		}
		
		if(dialect == null) {
			dialect = new MySQL();
			log.error("The wrong database type setting, please recheck your configure. The default sql style will be MySQL.");
		}
	}

	public static class BoundSqlSqlSource implements SqlSource {
		private BoundSql boundSql;

		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}

		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}
}