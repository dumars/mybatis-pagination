package com.github.dumars.mybatis.pagination.entity;

public class PageContext {
	
	private static final ThreadLocal<Page> PAGE_CONTEXT = new ThreadLocal<Page>();
	
	private PageContext() {
	}
	
	public static Page get() {
		Page page = PAGE_CONTEXT.get();
		if(page == null) {
			page = new Page(Page.DEFAULT_NO, Page.DEFAULT_SZIE, Integer.MAX_VALUE);
			PAGE_CONTEXT.set(page);
		}
		return page;
	}
	
	public static void set(Page page) {
		PAGE_CONTEXT.set(page);
	}
	
	public static void remove() {
		PAGE_CONTEXT.remove();
	}
}
