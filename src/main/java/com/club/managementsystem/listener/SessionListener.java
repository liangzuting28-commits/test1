package com.club.managementsystem.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

@WebListener
public class SessionListener implements HttpSessionListener {

    private static final AtomicInteger activeSessions = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        int count = activeSessions.incrementAndGet();
        System.out.println("[SessionListener] 会话创建: " + event.getSession().getId()
                + ", 当前活跃会话数: " + count);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        int count = activeSessions.decrementAndGet();
        System.out.println("[SessionListener] 会话销毁: " + event.getSession().getId()
                + ", 当前活跃会话数: " + count);
    }

    public static int getActiveSessions() {
        return activeSessions.get();
    }
}
