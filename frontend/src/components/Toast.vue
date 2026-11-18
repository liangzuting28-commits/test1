<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast-item"
        :class="'toast-' + toast.type"
        @click="removeToast(toast.id)"
      >
        <i class="toast-icon" :class="iconClass(toast.type)"></i>
        <span class="toast-message">{{ toast.message }}</span>
        <button class="toast-close" @click.stop="removeToast(toast.id)">&times;</button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useToastStore } from '../stores/toast'

const toastStore = useToastStore()
const { toasts } = storeToRefs(toastStore)
const { removeToast } = toastStore

function iconClass(type) {
  const map = {
    success: 'fas fa-check-circle',
    error: 'fas fa-times-circle',
    warning: 'fas fa-exclamation-triangle',
    info: 'fas fa-info-circle'
  }
  return map[type] || map.info
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  min-width: 280px;
}

.toast-success { background: linear-gradient(135deg, #52c41a, #73d13d); }
.toast-error { background: linear-gradient(135deg, #f5222d, #ff4d4f); }
.toast-warning { background: linear-gradient(135deg, #faad14, #ffc53d); }
.toast-info { background: linear-gradient(135deg, #667eea, #764ba2); }

.toast-icon { font-size: 18px; flex-shrink: 0; }
.toast-message { flex: 1; line-height: 1.4; }

.toast-close {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.8);
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  margin-left: 8px;
  flex-shrink: 0;
}
.toast-close:hover { color: white; }

.toast-enter-active { transition: all 0.3s ease; }
.toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from { opacity: 0; transform: translateX(80px); }
.toast-leave-to { opacity: 0; transform: translateX(80px) scale(0.9); }
.toast-move { transition: transform 0.3s ease; }
</style>
