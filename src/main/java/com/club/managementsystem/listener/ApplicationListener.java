package com.club.managementsystem.listener;
//应用启动时打印启动信息和 Servlet 版本，关闭时打印关闭日志
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[ApplicationListener] 应用启动: " + sce.getServletContext().getContextPath());
        System.out.println("[ApplicationListener] Servlet 版本: " + sce.getServletContext().getMajorVersion() + "." + sce.getServletContext().getMinorVersion());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[ApplicationListener] 应用关闭: " + sce.getServletContext().getContextPath());
    }
}
