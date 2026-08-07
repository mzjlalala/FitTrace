import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  apiGetProfile,
  apiLogin,
  apiLogout,
  apiRegister,
  type LoginParams,
  type RegisterParams,
  type UserInfo,
} from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('fitness_token') || '')
  const user = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('fitness_token', t)
  }

  async function login(params: LoginParams) {
    const res = await apiLogin(params)
    setToken(res.data.token)
    user.value = res.data.user
  }

  async function register(params: RegisterParams) {
    await apiRegister(params)
    // 注册成功后自动登录
    await login({ username: params.username, password: params.password })
  }

  async function fetchUser() {
    const res = await apiGetProfile()
    user.value = res.data
  }

  function logout() {
    // 先同步清空本地状态，保证立即退出（不依赖服务端响应，避免跳转被守卫拦截）
    token.value = ''
    user.value = null
    localStorage.removeItem('fitness_token')
    // 服务端黑名单请求尽力执行，失败不影响前端退出
    apiLogout().catch(() => {})
  }

  return { token, user, isLoggedIn, isAdmin, login, register, fetchUser, logout }
})
