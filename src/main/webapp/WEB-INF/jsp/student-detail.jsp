<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>学生详情</title>
    <style>
        body { font-family: 'Microsoft YaHei', sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; }
        h1 { color: #333; }
        .card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 15px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .card h3 { margin-top: 0; color: #667eea; }
        .info-row { display: flex; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
        .info-label { width: 120px; color: #666; font-weight: bold; }
        .info-value { flex: 1; color: #333; }
        .role-badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; }
        .role-STUDENT { background: #e3f2fd; color: #1565c0; }
        .role-PRESIDENT { background: #fff3e0; color: #e65100; }
        .role-TEACHER { background: #e8f5e9; color: #2e7d32; }
        .role-ADMIN { background: #fce4ec; color: #c62828; }
        .nav-link { display: inline-block; margin: 10px 10px 0 0; padding: 8px 16px; background: #667eea; color: white; text-decoration: none; border-radius: 6px; }
        .error { color: #c62828; background: #fce4ec; padding: 20px; border-radius: 8px; }
    </style>
</head>
<body>
<div class="container">
    <h1>学生详情</h1>

    <!-- JSTL 标签1：c:if -->
    <c:if test="${found}">
        <div class="card">
            <h3>基本信息</h3>
            <!-- EL 表达式1：属性访问（嵌套属性） -->
            <div class="info-row">
                <span class="info-label">学号</span>
                <span class="info-value">${student.studentId}</span>
            </div>
            <div class="info-row">
                <span class="info-label">姓名</span>
                <span class="info-value">${student.name}</span>
            </div>
            <div class="info-row">
                <span class="info-label">学院</span>
                <span class="info-value">${student.college}</span>
            </div>
            <div class="info-row">
                <span class="info-label">班级</span>
                <span class="info-value">${student.majorClass}</span>
            </div>
            <div class="info-row">
                <span class="info-label">角色</span>
                <span class="info-value">
                    <!-- JSTL 标签2：c:choose -->
                    <c:choose>
                        <c:when test="${student.role == 'STUDENT'}">
                            <span class="role-badge role-STUDENT">普通学生</span>
                        </c:when>
                        <c:when test="${student.role == 'PRESIDENT'}">
                            <span class="role-badge role-PRESIDENT">社团社长</span>
                        </c:when>
                        <c:when test="${student.role == 'TEACHER'}">
                            <span class="role-badge role-TEACHER">指导老师</span>
                        </c:when>
                        <c:otherwise>
                            <span class="role-badge role-ADMIN">系统管理员</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="info-row">
                <span class="info-label">积分</span>
                <!-- EL 表达式2：算术运算 -->
                <span class="info-value">${student.totalPoints} 分</span>
            </div>
            <div class="info-row">
                <span class="info-label">邮箱</span>
                <span class="info-value">${student.email}</span>
            </div>
            <div class="info-row">
                <span class="info-label">电话</span>
                <span class="info-value">${student.phone}</span>
            </div>
        </div>

        <!-- JSTL 标签3：c:set + c:if 组合 -->
        <c:set var="pointsLevel" value="${student.totalPoints >= 10 ? '优秀' : student.totalPoints >= 5 ? '良好' : '普通'}" />
        <div class="card">
            <h3>积分等级</h3>
            <!-- EL 表达式3：三元表达式嵌套 -->
            <p>当前积分：<strong>${student.totalPoints}</strong>，等级：<strong>${pointsLevel}</strong></p>
        </div>
    </c:if>

    <c:if test="${!found}">
        <div class="error">
            <h3>未找到该学生信息</h3>
            <p>请检查学号是否正确。</p>
        </div>
    </c:if>

    <a class="nav-link" href="${pageContext.request.contextPath}/jsp/clubs">返回社团列表</a>
    <a class="nav-link" href="${pageContext.request.contextPath}/index.html">返回首页</a>
</div>
</body>
</html>
