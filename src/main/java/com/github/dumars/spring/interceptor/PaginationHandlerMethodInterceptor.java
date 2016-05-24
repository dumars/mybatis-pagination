package com.github.dumars.spring.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.github.dumars.mybatis.pagination.annotation.Pagination;
import com.github.dumars.mybatis.pagination.entity.Page;
import com.github.dumars.mybatis.pagination.entity.PageContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaginationHandlerMethodInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("PaginationHandlerMethodInterceptor start...");
		}

		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			Pagination pagination = handlerMethod.getMethodAnnotation(Pagination.class);
			if (pagination != null) {
				String no = request.getParameter(Pagination.PAGE_NO_NAME);
				String size = request.getParameter(Pagination.PAGE_SIZE_NAME);
				int pageNo = StringUtils.isEmpty(no) ? Page.DEFAULT_NO : Integer.parseInt(no);
				int pageSize = StringUtils.isEmpty(size) ? Page.DEFAULT_SZIE : Integer.parseInt(size);
				Page page = new Page(pageNo, pageSize);
				PageContext.set(page);

				if (log.isDebugEnabled()) {
					log.debug("Find the Pagination annotation and setup page value.");
				}
			}
		}
		return true;
	}
}
