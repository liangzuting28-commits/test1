<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <style>
        body { font-family: 'Microsoft YaHei', sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; }
        h1 { color: #333; }
        .stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; margin-bottom: 20px; }
        .stat-card { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); text-align: center; }
        .stat-card .number { font-size: 36px; font-weight: bold; color: #667eea; }
        .stat-card .label { color: #666; margin-top: 5px; }
        .card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 15px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .card h3 { margin-top: 0; color: #667eea; }
        .nav-link { display: inline-block; margin: 10px 10px 0 0; padding: 8px 16px; background: #667eea; color: white; text-decoration: none; border-radius: 6px; }
        .status-good { color: #2e7d32; }
        .status-warn { color: #e65100; }
    </style>
</head>
<body>
<div class="container">
    <!-- EL 表达式1：属性访问 -->
    <h1>${title}</h1>

    <div class="stats-grid">
        <div class="stat-card">
            <!-- EL 表达式2：算术运算 -->
            <div class="number">${studentCount}</div>
            <div class="label">注册学生数</div>
        </div>
        <div class="stat-card">
            <div class="number">${clubCount}</div>
            <div class="label">社团总数</div>
        </div>
    </div>

    <!-- JSTL 标签1：c:if -->
    <c:if test="${studentCount > 0 && clubCount > 0}">
        <div class="card">
            <h3>系统状态</h3>
            <!-- EL 表达式3：比较运算 -->
            <p class="${studentCount > 5 ? 'status-good' : 'status-warn'}">
                学生数量：${studentCount}，
                <c:choose>
                    <c:when test="${studentCount > 5}">系统运行良好</c:when>
                    <c:otherwise>数据量较少，建议补充测试数据</c:otherwise>
                </c:choose>
            </p>
        </div>
    </c:if>

    <!-- JSTL 标签2：c:forEach 简单计数循环 -->
    <div class="card">
        <h3>快速导航</h3>
        <c:forEach var="i" begin="1" end="${clubCount}">
            <a class="nav-link" href="${pageContext.request.contextPath}/jsp/clubs">社团 ${i}</a>
        </c:forEach>
    </div>

    <!-- JSTL 标签3：c:set + EL 隐式对象 -->
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <div class="card">
        <h3>系统信息</h3>
        <p>Context Path: ${ctx}</p>
        <p>渲染技术：JSP + EL 表达式 + JSTL 标签库</p>
        <p>Session ID: ${pageContext.session.id}</p>
    </div>

    <a class="nav-link" href="${ctx}/jsp/clubs">社团列表</a>
    <a class="nav-link" href="${ctx}/index.html">返回首页</a>
</div>
</body>
</html>
