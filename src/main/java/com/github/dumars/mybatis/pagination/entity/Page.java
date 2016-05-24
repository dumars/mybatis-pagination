package com.github.dumars.mybatis.pagination.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ibatis.session.RowBounds;

public class Page extends RowBounds implements Serializable {

	private static final long serialVersionUID = -3805529090535665155L;
	public final static int DEFAULT_SZIE = 20;
	public final static int DEFAULT_NO = 0;
	private int pageSize;
	private int pageNo;
	private int totalRows;
	public final static Page DEFAULT = new Page();
	
	private Page() {
		super(0, DEFAULT_SZIE);
		this.pageNo = DEFAULT_NO;
		this.pageSize = DEFAULT_SZIE;
		this.totalRows = NO_ROW_LIMIT;
	}
	
	public Page(int pageNo) {
		super(getOffset(computePageNumber(pageNo, DEFAULT_SZIE, NO_ROW_LIMIT), DEFAULT_SZIE),
				getLimit(pageNo, DEFAULT_SZIE, NO_ROW_LIMIT));
		this.pageSize = DEFAULT_SZIE;
		this.totalRows = NO_ROW_LIMIT;
		this.pageNo = computePageNo(pageNo);
	}
	
	public Page(int pageNo, int pageSize) {
		super(getOffset(computePageNumber(pageNo, pageSize, NO_ROW_LIMIT), pageSize),
				getLimit(pageNo, pageSize, NO_ROW_LIMIT));
		this.pageSize = pageSize;
		this.totalRows = NO_ROW_LIMIT;
		this.pageNo = computePageNo(pageNo);
	}
	
	public Page(int pageNo, int pageSize, int totalRows) {
		super(getOffset(pageNo, pageSize), getLimit(pageNo, pageSize, totalRows));
		this.pageSize = pageSize;
		this.totalRows = totalRows;
		this.pageNo = computePageNo(pageNo);
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
	
	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public boolean isFirstPage() {
		return pageNo <= 0;
	}

	public boolean isLastPage() {
		return pageNo >= getTotalPages();
	}

	public int getPreviousPage() {
		if (hasPreviousPage()) {
			return pageNo - 1;
		} else {
			return pageNo;
		}
	}

	public int getNextPage() {
		if (hasNextPage()) {
			return pageNo + 1;
		} else {
			return pageNo;
		}
	}

	public boolean isDisabledPage(int pageNo) {
		return ((pageNo < 0) || (pageNo > getTotalPages()) || (pageNo == this.pageNo));
	}

	public boolean hasPreviousPage() {
		return (pageNo - 1 >= 0);
	}

	public boolean hasNextPage() {
		return (pageNo + 1 <= getTotalPages());
	}

	public int getStartRow() {
		if (pageSize <= 0 || totalRows <= 0) {
			return 0;
		} else {
			return (pageNo - 1) * pageSize + 1;
		}
	}

	public int getEndRow() {
		return pageNo >= 0 ? Math.min(pageSize * pageNo, totalRows) : 0;
	}

	@Override
	public int getOffset() {
		return getOffset(pageNo, pageSize);
	}
	
	private static int getOffset(int pageNo, int pageSize) {
		return pageNo >= 0 ? pageNo * pageSize : 0;
	}

	@Override
	public int getLimit() {
		return getLimit(pageNo, pageSize, totalRows);
	}
	
	private static int getLimit(int pageNo, int pageSize, int totalRows) {
		if (pageNo >= 0) {
			return Math.min((pageNo + 1) * pageSize, totalRows);
		} else {
			return Math.min(pageSize, totalRows);
		}
	}

	public int getTotalPages() {
		if (totalRows <= 0 || pageSize <= 0) {
			return 0;
		}

		int count = totalRows / pageSize;
		if (totalRows % pageSize > 0) {
			count++;
		}
		return count;
	}

	protected int computePageNo(int pageNo) {
		return computePageNumber(pageNo, pageSize, totalRows);
	}

	private static int computeLastPageNumber(int pageSize, int totalRows) {
		if (pageSize <= 0) {
			return 1;
		} else {
			int result = totalRows % pageSize == 0 ? (totalRows / pageSize) : (totalRows / pageSize + 1);
			if (result <= 1)
				result = 1;
			return result;
		}
	}

	private static int computePageNumber(int pageNo, int pageSize, int totalRows) {
		if (pageNo <= 0) {
			return 0;
		}
		
		if (Integer.MAX_VALUE == pageNo || pageNo > computeLastPageNumber(pageSize, totalRows)) {
			return computeLastPageNumber(pageSize, totalRows);
		}
		
		return pageNo;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
