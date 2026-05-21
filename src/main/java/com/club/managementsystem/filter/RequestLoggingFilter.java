package com.club.managementsystem.filter;
//每个 API 请求创建和销毁时打印日志（只记录 /api/*，忽略静态资源）
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = "/api/*")
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        System.out.println("[API Request] " + httpRequest.getMethod() + " "
                + httpRequest.getRequestURI()
                + " from " + httpRequest.getRemoteAddr());

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        System.out.println("[API Response] " + httpRequest.getRequestURI()
                + " Status: " + httpResponse.getStatus()
                + " Duration: " + duration + "ms");
    }
}
