<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

const router = useRouter()

interface HealthInfo {
  db: string
  redis: string
}

const health = ref<HealthInfo | null>(null)

const entries = [
  { title: '动作库', desc: '健身动作教程与肌群指南', emoji: '🏋️', color: '#10b981', path: '/actions' },
  { title: '计划中心', desc: '科学的训练计划编排', emoji: '📋', color: '#3b82f6', path: '/plans' },
  { title: '训练记录', desc: '打卡、PR 与热力图统计', emoji: '📈', color: '#f59e0b', path: '/training' },
  { title: '饮食记录', desc: '营养摄入与热量管理', emoji: '🥗', color: '#ef4444', path: '/diet' },
]

onMounted(async () => {
  try {
    const res = await http.get<HealthInfo>('/health')
    health.value = res.data
  } catch {
    health.value = null
  }
})
</script>

<template>
  <div class="home">
    <section class="hero">
      <h1 class="hero-title">开始你的<span>健身之旅</span></h1>
      <p class="hero-sub">科学训练计划 · 动作教程 · 打卡统计 · 营养管理，一站式健身助手</p>
      <div class="hero-actions">
        <el-button type="primary" size="large" round @click="router.push('/actions')">浏览动作库</el-button>
        <el-button size="large" round class="hero-btn-ghost" @click="router.push('/training')">记录今日训练</el-button>
      </div>
    </section>

    <section class="entries">
      <el-card v-for="e in entries" :key="e.title" class="entry-card" shadow="hover" @click="router.push(e.path)">
        <div class="entry-emoji" :style="{ background: e.color + '1a' }">{{ e.emoji }}</div>
        <h3 class="entry-title">{{ e.title }}</h3>
        <p class="entry-desc">{{ e.desc }}</p>
        <span class="entry-more" :style="{ color: e.color }">进入 →</span>
      </el-card>
    </section>

    <el-card v-if="health" class="health-card">
      <template #header>
        <span class="health-title">🩺 服务状态</span>
      </template>
      <div class="health-row">
        <span class="health-label">后端数据库</span>
        <el-tag :type="health.db === 'up' ? 'success' : 'danger'">{{ health.db }}</el-tag>
      </div>
      <div class="health-row">
        <span class="health-label">Redis 缓存</span>
        <el-tag :type="health.redis === 'up' ? 'success' : 'danger'">{{ health.redis }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.home {
  max-width: 960px;
}

/* ===== Hero 品牌区 ===== */
.hero {
  position: relative;
  overflow: hidden;
  padding: 48px 40px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0d9467 0%, #10b981 45%, #22d3ee 100%);
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.25);
}
.hero::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -60px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}
.hero-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 12px;
}
.hero-title span {
  color: #fde68a;
}
.hero-sub {
  font-size: 15px;
  opacity: 0.92;
  margin-bottom: 28px;
}
.hero-actions {
  display: flex;
  gap: 12px;
}
.hero-btn-ghost {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.6);
  color: #fff;
}
.hero-btn-ghost:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: #fff;
  color: #fff;
}

/* ===== 功能入口 ===== */
.entries {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
@media (max-width: 900px) {
  .entries {
    grid-template-columns: repeat(2, 1fr);
  }
}
.entry-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.entry-card:hover {
  transform: translateY(-4px);
}
.entry-emoji {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  border-radius: 12px;
  margin-bottom: 12px;
}
.entry-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 6px;
}
.entry-desc {
  color: var(--text-2);
  font-size: 13px;
  margin-bottom: 12px;
}
.entry-more {
  font-size: 13px;
  font-weight: 500;
}

/* ===== 健康状态 ===== */
.health-card {
  max-width: 400px;
}
.health-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}
.health-label {
  color: var(--text-2);
  font-size: 14px;
}
</style>
