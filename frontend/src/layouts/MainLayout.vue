<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function onLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="200px">
      <div class="logo">FitTrace</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/actions">动作库</el-menu-item>
        <el-menu-item index="/plans">计划中心</el-menu-item>
        <el-menu-item index="/training">训练记录</el-menu-item>
        <el-menu-item index="/diet">饮食记录</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
        <el-sub-menu v-if="auth.isAdmin" index="admin">
          <template #title>管理后台</template>
          <el-menu-item index="/admin/actions">动作管理</el-menu-item>
          <el-menu-item index="/admin/plans">计划管理</el-menu-item>
          <el-menu-item index="/admin/foods">食物管理</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="slogan">健身教练 + 记录工具</span>
        <div class="header-right">
          <el-avatar v-if="auth.user?.avatar" :src="auth.user.avatar" :size="28" />
          <el-avatar v-else :size="28" class="avatar-fallback">
            {{ (auth.user?.nickname || auth.user?.username || 'U').charAt(0) }}
          </el-avatar>
          <span class="nickname">{{ auth.user?.nickname }}</span>
          <el-button link type="primary" @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-weight: 700;
  font-size: 20px;
  color: #409eff;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.nickname {
  color: #606266;
  font-size: 14px;
}
.avatar-fallback {
  background: #409eff;
}
</style>
