package com.club.managementsystem.listener;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;

@WebListener
public class RequestListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        String uri = request.getRequestURI();
        // 只记录 API 请求，忽略静态资源
        if (uri.startsWith("/api/")) {
            String sessionId = (request.getSession(false) != null) ? request.getSession(false).getId() : "无";
            System.out.println("[RequestListener] 请求创建: " + request.getMethod() + " " + uri
                    + ", SessionID: " + sessionId);
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/")) {
            System.out.println("[RequestListener] 请求销毁: " + request.getMethod() + " " + uri);
        }
    }
}
