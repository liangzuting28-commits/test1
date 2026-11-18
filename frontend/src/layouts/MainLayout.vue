<template>
  <div class="layout">
    <AppSidebar />
    <div class="main-area">
      <AppHeader />
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import AppSidebar from '../components/AppSidebar.vue'
import AppHeader from '../components/AppHeader.vue'
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}
.main-area {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
}
.content {
  flex: 1;
  padding: 24px;
}
</style>
