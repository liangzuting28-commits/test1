import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])
  let nextId = 0

  function addToast(message, type = 'info', duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    if (duration > 0) {
      setTimeout(() => removeToast(id), duration)
    }
    return id
  }

  function removeToast(id) {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  function success(message, duration = 3000) {
    return addToast(message, 'success', duration)
  }

  function error(message, duration = 4000) {
    return addToast(message, 'error', duration)
  }

  function warning(message, duration = 3000) {
    return addToast(message, 'warning', duration)
  }

  function info(message, duration = 3000) {
    return addToast(message, 'info', duration)
  }

  return { toasts, success, error, warning, info, removeToast }
})
