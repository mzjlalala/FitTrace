<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login({ ...form })
    ElMessage.success('登录成功')
    router.push((route.query.redirect as string) || '/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <div class="brand">
        <div class="brand-logo">💪</div>
        <div class="brand-text">
          <h2 class="title">FitTrace</h2>
          <p class="subtitle">一站式健身助手 · 欢迎回来</p>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
      <div class="foot">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0d9467 0%, #10b981 50%, #22d3ee 100%);
  overflow: hidden;
}
.auth-page::before,
.auth-page::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}
.auth-page::before {
  width: 320px;
  height: 320px;
  left: -100px;
  top: -100px;
}
.auth-page::after {
  width: 220px;
  height: 220px;
  right: -60px;
  bottom: -60px;
}
.auth-card {
  position: relative;
  width: 400px;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
}
.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}
.brand-logo {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  border-radius: 12px;
  background: linear-gradient(135deg, #10b981, #22d3ee);
}
.brand-text {
  text-align: left;
}
.title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #0d9467;
}
.subtitle {
  margin: 2px 0 0;
  font-size: 13px;
  color: #6b7280;
}
.submit {
  width: 100%;
  margin-top: 8px;
}
.foot {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}
</style>
