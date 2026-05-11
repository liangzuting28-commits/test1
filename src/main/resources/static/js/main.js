document.addEventListener('DOMContentLoaded', function() {
    console.log('🎯 页面加载完成，开始初始化...');
    console.log('📝 当前页面:', window.location.pathname);

    // 记录找到的元素
    const roleOptions = document.querySelectorAll('.role-option');
    const roleDesc = document.getElementById('roleDesc');
    const loginForm = document.getElementById('loginForm');
    const passwordToggle = document.getElementById('passwordToggle');
    const passwordInput = document.getElementById('password');
    const usernameInput = document.getElementById('username');
    const usernameError = document.getElementById('usernameError');
    const passwordError = document.getElementById('passwordError');

    console.log('🔍 找到的元素:', {
        roleOptions: roleOptions.length,
        roleDesc: !!roleDesc,
        loginForm: !!loginForm,
        passwordToggle: !!passwordToggle,
        usernameInput: !!usernameInput,
        passwordInput: !!passwordInput,
        usernameError: !!usernameError,
        passwordError: !!passwordError
    });

    const roleDescriptions = {
        STUDENT: '<strong>学生权限：</strong>浏览活动、报名参与、扫码签到、查询积分',
        PRESIDENT: '<strong>社长权限：</strong>创建活动、生成签到码、管理本社活动、查看统计数据',
        TEACHER: '<strong>团委老师权限：</strong>审批活动、制订积分规则、查看全局数据、管理社团',
        ADMIN: '<strong>系统管理员权限：</strong>管理所有用户、活动和社团，系统配置和维护'
    };

    console.log('✅ 角色描述数据加载完成');

    // 角色选择功能
    roleOptions.forEach(option => {
        option.addEventListener('click', function() {
            const selectedRole = this.getAttribute('data-role');
            console.log('👤 角色选择:', selectedRole);

            roleOptions.forEach(opt => opt.classList.remove('active'));
            this.classList.add('active');

            if (roleDesc) {
                roleDesc.innerHTML = roleDescriptions[selectedRole] || '';
                console.log('📋 更新角色描述:', selectedRole);
            }

            if (selectedRole === 'TEACHER') {
                usernameInput.placeholder = "请输入工号";
                console.log('👨‍🏫 设置为老师模式');
            } else {
                usernameInput.placeholder = "请输入学号";
                console.log('🎓 设置为学生/社长模式');
            }
        });
    });

    // 密码显示/隐藏功能
    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener('click', function() {
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                this.textContent = '🙈';
                console.log('👁️ 密码显示状态: 可见');
            } else {
                passwordInput.type = 'password';
                this.textContent = '👁️';
                console.log('👁️ 密码显示状态: 隐藏');
            }
        });
    }

    // 在 activity-manage.html 的 JavaScript 部分添加以下函数

    let activityTypes = [];

// 加载活动类型（从积分规则获取）
    async function loadActivityTypes() {
        try {
            const response = await fetch('/api/points/rules');
            const result = await response.json();

            if (result.success && result.data) {
                activityTypes = result.data;
                const activityTypeSelect = document.getElementById('activityType');
                activityTypeSelect.innerHTML = '<option value="">请选择活动类型</option>';

                activityTypes.forEach(type => {
                    const option = document.createElement('option');
                    option.value = type.activityType;
                    option.textContent = `${type.activityType} (${type.basePointsValue}积分)`;
                    option.setAttribute('data-points', type.basePointsValue);
                    activityTypeSelect.appendChild(option);
                });

                console.log('活动类型加载完成:', activityTypes);
            } else {
                console.error('获取活动类型失败:', result.message);
                // 使用默认活动类型作为备选
                loadDefaultActivityTypes();
            }
        } catch (error) {
            console.error('加载活动类型错误:', error);
            // 使用默认活动类型作为备选
            loadDefaultActivityTypes();
        }
    }

// 备选方案：使用默认活动类型
    function loadDefaultActivityTypes() {
        activityTypes = [
            { activityType: '学术讲座', basePointsValue: 5 },
            { activityType: '文体活动', basePointsValue: 3 },
            { activityType: '志愿公益', basePointsValue: 8 },
            { activityType: '竞赛获奖', basePointsValue: 10 },
            { activityType: '社会实践', basePointsValue: 6 }
        ];

        const activityTypeSelect = document.getElementById('activityType');
        activityTypeSelect.innerHTML = '<option value="">请选择活动类型</option>';

        activityTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type.activityType;
            option.textContent = `${type.activityType} (${type.basePointsValue}积分)`;
            option.setAttribute('data-points', type.basePointsValue);
            activityTypeSelect.appendChild(option);
        });
    }

// 根据选择的活动类型更新积分值
    function updateAwardPoints() {
        const activityTypeSelect = document.getElementById('activityType');
        const selectedOption = activityTypeSelect.options[activityTypeSelect.selectedIndex];
        const points = selectedOption.getAttribute('data-points');

        if (points) {
            document.getElementById('awardPoints').value = points;
        } else {
            document.getElementById('awardPoints').value = '0';
        }
    }

// 修改创建活动的函数，确保包含活动类型
    async function createActivity() {
        const activityName = document.getElementById('activityName').value.trim();
        const clubId = parseInt(document.getElementById('clubId').value);
        const activityType = document.getElementById('activityType').value;
        const activityTime = document.getElementById('activityTime').value;
        const location = document.getElementById('location').value.trim();
        const maxParticipants = parseInt(document.getElementById('maxParticipants').value);
        const awardPoints = parseInt(document.getElementById('awardPoints').value);
        const description = document.getElementById('description').value.trim();

        // 表单验证
        if (!activityName) {
            alert('请输入活动名称');
            return;
        }

        if (!clubId) {
            alert('请选择所属社团');
            return;
        }

        if (!activityType) {
            alert('请选择活动类型');
            return;
        }

        if (!activityTime) {
            alert('请选择活动时间');
            return;
        }

        if (!location) {
            alert('请输入活动地点');
            return;
        }

        if (!awardPoints || awardPoints < 1) {
            alert('请选择有效的活动类型以设置积分');
            return;
        }

        const formData = {
            activityName: activityName,
            clubId: clubId,
            activityType: activityType, // 添加活动类型
            activityTime: activityTime,
            location: location,
            maxParticipants: maxParticipants || 0,
            awardPoints: awardPoints,
            description: description
        };

        console.log('提交的活动数据:', formData);

        try {
            // 显示加载状态
            const submitBtn = document.querySelector('#createActivityForm button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 提交中...';
            submitBtn.disabled = true;

            const response = await fetch('/api/activity-manage/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': currentUser.studentId
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();
            console.log('创建活动响应:', result);

            if (result.success) {
                alert('🎉 活动创建成功！等待老师审核。');
                document.getElementById('createActivityForm').reset();
                document.getElementById('awardPoints').value = '0'; // 重置积分值
                loadMyActivities();
                switchTab('my-activities');
            } else {
                alert('❌ 创建失败: ' + result.message);
            }
        } catch (error) {
            console.error('创建活动错误:', error);
            alert('❌ 创建活动时发生错误，请稍后重试: ' + error.message);
        } finally {
            // 恢复按钮状态
            const submitBtn = document.querySelector('#createActivityForm button[type="submit"]');
            submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> 提交审核';
            submitBtn.disabled = false;
        }
    }

// 在社长界面设置中调用加载活动类型
    function setupRoleBasedUI() {
        // ... 其他代码保持不变 ...

        if (currentUser.role === 'PRESIDENT') {
            // ... 其他代码保持不变 ...

            // 加载社长管理的社团
            loadPresidentClubs();
            // 加载活动类型
            loadActivityTypes();

            setupEventListeners();
            switchTab('create');
            console.log('设置为社长界面');
        }
        // ... 其他代码保持不变 ...
    }

    // 表单验证功能
    function validateForm() {
        console.log('🔍 开始表单验证...');
        let isValid = true;

        if (!usernameInput.value.trim()) {
            console.log('❌ 用户名验证失败: 为空');
            if (usernameError) {
                usernameError.style.display = 'block';
            }
            usernameInput.style.borderColor = '#e74c3c';
            isValid = false;
        } else {
            console.log('✅ 用户名验证成功');
            if (usernameError) {
                usernameError.style.display = 'none';
            }
            usernameInput.style.borderColor = '#ddd';
        }

        if (!passwordInput.value.trim()) {
            console.log('❌ 密码验证失败: 为空');
            if (passwordError) {
                passwordError.style.display = 'block';
            }
            passwordInput.style.borderColor = '#e74c3c';
            isValid = false;
        } else {
            console.log('✅ 密码验证成功');
            if (passwordError) {
                passwordError.style.display = 'none';
            }
            passwordInput.style.borderColor = '#ddd';
        }

        console.log(`📊 表单验证结果: ${isValid ? '通过' : '失败'}`);
        return isValid;
    }

    // 显示全局消息
    function showMessage(message, isSuccess = true) {
        console.log(`💬 显示消息: ${message} (${isSuccess ? '成功' : '错误'})`);

        // 移除现有消息
        const existingMessage = document.querySelector('.success-message, .error-message-global');
        if (existingMessage) {
            existingMessage.remove();
            console.log('🗑️ 移除现有消息');
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = isSuccess ? 'success-message' : 'error-message-global';
        messageDiv.textContent = message;
        messageDiv.style.display = 'block';

        if (loginForm && loginForm.parentNode) {
            loginForm.parentNode.insertBefore(messageDiv, loginForm);
            console.log('📍 消息插入位置: 表单前');
        }

        // 5秒后自动消失
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.remove();
                console.log('⏰ 自动移除消息');
            }
        }, 5000);
    }

    // 表单提交处理
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            console.log('🚀 表单提交开始...');

            if (validateForm()) {
                const username = usernameInput.value;
                const password = passwordInput.value;
                const activeRole = document.querySelector('.role-option.active');

                if (!activeRole) {
                    console.log('❌ 表单提交失败: 未选择角色');
                    showMessage('❌ 请选择角色', false);
                    return;
                }

                const selectedRole = activeRole.getAttribute('data-role');

                console.log('📤 提交数据:', {
                    username: username,
                    password: '***' + password.slice(-2), // 隐藏密码，只显示最后2位
                    selectedRole: selectedRole
                });

                // 显示加载状态
                const loginBtn = document.querySelector('.login-btn');
                const originalText = loginBtn.textContent;
                loginBtn.textContent = '登录中...';
                loginBtn.disabled = true;
                loginForm.classList.add('loading');

                console.log('⏳ 显示加载状态');

                try {
                    // 向后端发送登录请求
                    console.log('🌐 发送登录请求到 /api/auth/login');
                    const startTime = Date.now();

                    const response = await fetch('/api/auth/login', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            username: username,
                            password: password,
                            role: selectedRole
                        })
                    });

                    const endTime = Date.now();
                    console.log(`⏱️ 请求耗时: ${endTime - startTime}ms`);
                    console.log('📥 收到响应:', response.status);

                    const result = await response.json();
                    console.log('📊 响应数据:', result);

                    if (result.success) {
                        console.log('✅ 登录成功');
                        showMessage('✅ ' + result.message, true);

                        // 保存token和用户信息到localStorage
                        if (result.data && result.data.token) {
                            localStorage.setItem('token', result.data.token);
                            localStorage.setItem('user', JSON.stringify(result.data.user));
                            console.log('💾 保存用户信息到 localStorage');
                            console.log('👤 用户信息:', result.data.user);

                            // 延迟跳转，让用户看到成功消息
                            setTimeout(() => {
                                // 根据角色跳转到不同页面
                                if (result.data.user.role === 'ADMIN') {
                                    console.log('🔀 跳转到管理员仪表盘');
                                    window.location.href = '/admin-dashboard.html';
                                } else {
                                    console.log('🔀 跳转到用户仪表盘');
                                    window.location.href = '/dashboard.html';
                                }
                            }, 1500);
                        } else {
                            console.log('❌ 登录响应数据不完整');
                            showMessage('❌ 登录响应数据不完整', false);
                        }

                    } else {
                        console.log('❌ 登录失败:', result.message);
                        showMessage('❌ ' + result.message, false);
                    }
                } catch (error) {
                    console.error('💥 登录错误:', error);
                    showMessage('❌ 登录过程中发生错误，请检查网络连接: ' + error.message, false);
                } finally {
                    // 重置按钮状态
                    loginBtn.textContent = originalText;
                    loginBtn.disabled = false;
                    loginForm.classList.remove('loading');
                    console.log('🔄 重置按钮状态');
                }
            } else {
                console.log('❌ 表单验证失败，停止提交');
            }
        });
    }

    // 输入时实时验证
    if (usernameInput) {
        usernameInput.addEventListener('input', function() {
            if (this.value.trim()) {
                console.log('📝 用户名输入中...');
                if (usernameError) {
                    usernameError.style.display = 'none';
                }
                this.style.borderColor = '#ddd';
            }
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            if (this.value.trim()) {
                console.log('🔑 密码输入中...');
                if (passwordError) {
                    passwordError.style.display = 'none';
                }
                this.style.borderColor = '#ddd';
            }
        });
    }

    // 测试后端连接
    async function testBackendConnection() {
        try {
            console.log('🔗 测试后端连接...');
            const startTime = Date.now();

            const response = await fetch('/api/auth/test');
            const endTime = Date.now();

            const result = await response.json();
            console.log(`✅ 后端连接测试成功:`, {
                status: response.status,
                responseTime: `${endTime - startTime}ms`,
                data: result
            });
        } catch (error) {
            console.error('❌ 后端连接失败:', {
                error: error.message,
                url: '/api/auth/test'
            });
        }
    }

    // 页面加载时测试连接
    testBackendConnection();

    // 添加额外的调试信息
    console.log('🔧 调试功能已启用');
    console.log('📍 当前时间:', new Date().toLocaleString());
    console.log('🌐 用户代理:', navigator.userAgent);
    console.log('💻 屏幕分辨率:', `${screen.width}x${screen.height}`);

    // 监听页面卸载事件
    window.addEventListener('beforeunload', function() {
        console.log('🚪 页面即将关闭或刷新');
    });

    console.log('🎉 初始化完成');
});

// 添加全局错误监听
window.addEventListener('error', function(e) {
    console.error('💥 全局错误捕获:', {
        message: e.message,
        filename: e.filename,
        lineno: e.lineno,
        colno: e.colno,
        error: e.error
    });
});

// 添加未处理的Promise拒绝监听
window.addEventListener('unhandledrejection', function(e) {
    console.error('💥 未处理的Promise拒绝:', {
        reason: e.reason,
        promise: e.promise
    });
});

// 调试工具函数
window.debugUtils = {
    // 显示所有localStorage内容
    showStorage: function() {
        console.log('💾 localStorage内容:', {
            token: localStorage.getItem('token') ? '***' + localStorage.getItem('token').slice(-10) : '空',
            user: JSON.parse(localStorage.getItem('user') || '{}')
        });
    },

    // 清除所有存储
    clearStorage: function() {
        localStorage.clear();
        console.log('🗑️ 已清除所有localStorage');
    },

    // 模拟登录
    simulateLogin: function(role = 'STUDENT') {
        const testUsers = {
            STUDENT: { username: '2021001001', password: '123456' },
            PRESIDENT: { username: '2020001001', password: '123456' },
            TEACHER: { username: 'T001', password: '123456' },
            ADMIN: { username: 'ADMIN001', password: '123456' }
        };

        const user = testUsers[role];
        if (user) {
            document.getElementById('username').value = user.username;
            document.getElementById('password').value = user.password;
            document.querySelector(`[data-role="${role}"]`).click();
            console.log(`🎭 模拟${role}登录:`, user);
        }
    },

    // 获取页面性能信息
    getPerformance: function() {
        const perf = performance.timing;
        console.log('📊 页面性能数据:', {
            'DNS查询': perf.domainLookupEnd - perf.domainLookupStart + 'ms',
            'TCP连接': perf.connectEnd - perf.connectStart + 'ms',
            '请求响应': perf.responseEnd - perf.requestStart + 'ms',
            'DOM解析': perf.domComplete - perf.domInteractive + 'ms',
            '页面加载': perf.loadEventEnd - perf.navigationStart + 'ms'
        });
    }
};

console.log('🔧 调试工具已加载，使用 window.debugUtils 访问');