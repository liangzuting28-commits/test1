<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <style>
        body { font-family: 'Microsoft YaHei', sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: 0 auto; }
        h1 { color: #333; }
        .info-bar { background: #667eea; color: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        th { background: #667eea; color: white; padding: 12px 15px; text-align: left; }
        td { padding: 12px 15px; border-bottom: 1px solid #eee; }
        tr:hover { background: #f8f9ff; }
        .badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; }
        .badge-admin { background: #e8f5e9; color: #2e7d32; }
        .badge-user { background: #e3f2fd; color: #1565c0; }
        .nav-link { display: inline-block; margin: 10px 10px 0 0; padding: 8px 16px; background: #667eea; color: white; text-decoration: none; border-radius: 6px; }
        .nav-link:hover { background: #5a6fd6; }
    </style>
</head>
<body>
<div class="container">
    <!-- EL 表达式1：属性访问 -->
    <h1>${title}</h1>

    <!-- JSTL 标签1：c:set 设置变量 -->
    <c:set var="systemName" value="社团活动管理系统" />

    <div class="info-bar">
        <!-- EL 表达式2：算术运算 -->
        <p>系统：${systemName} | 社团总数：${clubCount} | 页面渲染方式：JSP + EL + JSTL</p>
        <!-- EL 表达式3：三元表达式 -->
        <p>当前身份：<span class="${isAdmin ? 'badge badge-admin' : 'badge badge-user'}">${isAdmin ? '管理员' : '普通用户'}</span></p>
    </div>

    <!-- JSTL 标签2：c:if 条件渲染 -->
    <c:if test="${clubCount > 0}">
        <p style="color: #2e7d32;">共有 ${clubCount} 个社团可供浏览</p>
    </c:if>

    <!-- JSTL 标签3：c:forEach 循环遍历 -->
    <table>
        <thead>
            <tr>
                <th>序号</th>
                <th>社团名称</th>
                <th>社团类型</th>
                <th>社团简介</th>
                <th>社长ID</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="club" items="${clubs}" varStatus="status">
                <tr>
                    <!-- EL 表达式4：隐式对象 -->
                    <td>${status.index + 1}</td>
                    <td><strong>${club.clubName}</strong></td>
                    <td>${club.clubType}</td>
                    <td>${club.introduction}</td>
                    <td>${club.presidentId}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <!-- JSTL 标签4：c:choose 多分支条件 -->
    <div style="margin-top: 20px;">
        <c:choose>
            <c:when test="${isAdmin}">
                <a class="nav-link" href="${pageContext.request.contextPath}/admin-dashboard.html">进入管理面板</a>
                <a class="nav-link" href="${pageContext.request.path}/jsp/dashboard">查看统计面板</a>
            </c:when>
            <c:otherwise>
                <a class="nav-link" href="${pageContext.request.contextPath}/dashboard.html">进入用户面板</a>
            </c:otherwise>
        </c:choose>
        <a class="nav-link" href="${pageContext.request.contextPath}/index.html">返回首页</a>
    </div>

    <!-- EL 表达式5：隐式对象 pageContext -->
    <p style="margin-top: 20px; color: #999; font-size: 12px;">
        Context Path: ${pageContext.request.contextPath} | 当前时间: <%= new java.util.Date() %>
    </p>
</div>
</body>
</html>
