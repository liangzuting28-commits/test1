// 退出社团
async function leaveClub(clubId) {
    // 创建一个简单的模态框
    const modalHtml = `
        <div id="leaveModal" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center;">
            <div style="background-color: white; padding: 20px; border-radius: 8px; width: 80%; max-width: 400px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
                <h3 style="margin-top: 0; color: #333;">申请退出社团</h3>
                <p>请输入退出原因（必填）:</p>
                <textarea id="leaveReason" rows="4" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-bottom: 15px; resize: vertical;" placeholder="请详细说明您的退出原因..."></textarea>
                <div style="text-align: right;">
                    <button id="cancelBtn" class="btn" style="background-color: #f0f0f0; color: #333; margin-right: 10px;">取消</button>
                    <button id="submitBtn" class="btn btn-primary">提交申请</button>
                </div>
            </div>
        </div>
    `;

    // 添加到页面
    document.body.insertAdjacentHTML('beforeend', modalHtml);

    // 获取元素
    const modal = document.getElementById('leaveModal');
    const cancelBtn = document.getElementById('cancelBtn');
    const submitBtn = document.getElementById('submitBtn');

    // 关闭模态框
    const closeModal = () => {
        document.body.removeChild(modal);
    };

    // 事件监听
    cancelBtn.addEventListener('click', closeModal);

    // 提交申请
    submitBtn.addEventListener('click', async () => {
        const reason = document.getElementById('leaveReason').value.trim();
        if (!reason) {
            alert('请输入退出原因');
            return;
        }

        try {
            const response = await fetch(`/api/member-manage/club/${clubId}/member/${currentUser.studentId}/request-leave`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': currentUser.studentId
                },
                body: JSON.stringify({
                    reason: reason
                })
            });

            const result = await response.json();
            console.log('API响应:', result); // 添加调试日志

            if (result.success) {
                closeModal();
                showNotification('退出申请已提交，等待社长审批', 'success');
                loadMyClubs(); // 重新加载社团列表
            } else {
                showNotification('申请失败: ' + (result.message || '未知错误'), 'error');
            }
        } catch (error) {
            console.error('申请退出社团错误:', error);
            showNotification('申请退出社团时发生错误: ' + error.message, 'error');
        }
    });
}
