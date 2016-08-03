package com.github.dumars.mybatis.pagination.entity;

public class Paginator {
	
	private int pageNo;
	private int pageSize;
	private int totalRows;
	private Object items;
	
	public Paginator(Object items) {
		this.items = items;
		
		Page page = PageContext.get();
		this.pageNo = page.getPageNo();
		this.pageSize = page.getPageSize();
		this.totalRows = page.getTotalRows();
	}
	
	public Paginator(Object items, int pageNo, int pageSize, int totalRows) {
		this.items = items;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.totalRows = totalRows;
	}
	
	public int getPageNo() {
		return pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public int getTotalRows() {
		return totalRows;
	}
	public Object getItems() {
		return items;
	}
	public void setItems(Object items) {
		this.items = items;
	}
}
