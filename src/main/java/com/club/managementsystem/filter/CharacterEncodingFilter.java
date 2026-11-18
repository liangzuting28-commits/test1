package com.club.managementsystem.filter;
// 拦截所有请求，强制设置请求和响应的编码为 UTF-8，防止中文乱码
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 设置请求编码为 UTF-8
        httpRequest.setCharacterEncoding("UTF-8");

        // 设置响应编码为 UTF-8
        httpResponse.setCharacterEncoding("UTF-8");

        chain.doFilter(request, response);
    }
}
