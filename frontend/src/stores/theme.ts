import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'fitness_theme'

/** 应用暗色类到 <html>，供 Element Plus dark css-vars 与自定义变量生效 */
function applyTheme(dark: boolean) {
  document.documentElement.classList.toggle('dark', dark)
}

export const useThemeStore = defineStore('theme', () => {
  // 初始化：优先读本地存储，默认亮色
  const dark = ref(localStorage.getItem(STORAGE_KEY) === 'dark')
  applyTheme(dark.value)

  function toggle() {
    dark.value = !dark.value
    localStorage.setItem(STORAGE_KEY, dark.value ? 'dark' : 'light')
    applyTheme(dark.value)
  }

  return { dark, toggle }
})
