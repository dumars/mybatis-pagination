package com.github.dumars.mybatis.pagination.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pagination {
	
	public final static String PAGE_NO_NAME = "pageNo";
	public final static String PAGE_SIZE_NAME = "pageSize";
	public final static String ITEM_NAME = "items";
	
	String pageNoName() default PAGE_NO_NAME;
	String pageSizeName() default PAGE_SIZE_NAME;
}