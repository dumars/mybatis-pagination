package com.github.dumars.mybatis.pagination.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.reflections.Reflections;

import com.github.dumars.mybatis.pagination.dialect.Dialect;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MybatisUtils {

	public static int count(final String sql, final Connection connection,
			final MappedStatement state, final Object parameter, final BoundSql boundSql,
			Dialect dialect) throws SQLException {

		final String countingSQL = dialect.generateCountSQL(sql);
		
		@Cleanup PreparedStatement statement = connection.prepareStatement(countingSQL);
		final BoundSql countBS = new BoundSql(state.getConfiguration(), countingSQL,
				boundSql.getParameterMappings(), parameter);
		setParameters(statement, state, countBS, parameter);
		
		@Cleanup ResultSet resultSet = statement.executeQuery();
		int count = 0;
		if (resultSet.next()) {
			count = resultSet.getInt(1);
		}
		
		log.debug("execute count, the total rows are {} ", count);
		
		return count;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setParameters(PreparedStatement stmt, MappedStatement state,
			BoundSql boundSql, Object parameterObject) throws SQLException {
		ErrorContext.instance().activity("setting parameters").object(
				state.getParameterMap().getId());
		
		List<ParameterMapping> list = boundSql.getParameterMappings();
		
		if (list != null) {
			Configuration configuration = state.getConfiguration();
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			MetaObject metaObject = parameterObject == null ? null : configuration
					.newMetaObject(parameterObject);
			for (int i = 0; i < list.size(); i++) {
				ParameterMapping parameterMapping = list.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					PropertyTokenizer prop = new PropertyTokenizer(propertyName);
					if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)
							&& boundSql.hasAdditionalParameter(prop.getName())) {
						value = boundSql.getAdditionalParameter(prop.getName());
						if (value != null) {
							value = configuration.newMetaObject(value).getValue(
									propertyName.substring(prop.getName().length()));
						}
					} else {
						value = metaObject == null ? null : metaObject.getValue(propertyName);
					}
					
					TypeHandler typeHandler = parameterMapping.getTypeHandler();
					if (typeHandler == null) {
						throw new ExecutorException("There was no TypeHandler found for parameter "
								+ propertyName + " of statement " + state.getId());
					}
					
					typeHandler.setParameter(stmt, i + 1, value, parameterMapping.getJdbcType());
				}
			}
		}
	}
	
	public static Map<String, Class<? extends Dialect>> getSupportDatabaseType() {
		Map<String, Class<? extends Dialect>> map = new HashMap<String, Class<? extends Dialect>>();
		
		Reflections reflections = new Reflections(Dialect.class.getPackage().getName());
		Set<Class<? extends Dialect>> dialects = reflections.getSubTypesOf(Dialect.class);
		for(Class<? extends Dialect> dialect : dialects) {
			map.put(dialect.getSimpleName().toUpperCase(), dialect);
		}
		
		return map;
	}
}
