<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/api/http'

interface HealthInfo {
  db: string
  redis: string
}

const health = ref<HealthInfo | null>(null)

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
  <div>
    <h2>欢迎使用 FitTrace</h2>
    <p>一站式健身网站：训练计划、动作教程、训练记录、数据分析。</p>
    <el-descriptions v-if="health" title="服务健康状态" :column="2" border>
      <el-descriptions-item label="后端数据库">
        <el-tag :type="health.db === 'up' ? 'success' : 'danger'">{{ health.db }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="Redis">
        <el-tag :type="health.redis === 'up' ? 'success' : 'danger'">{{ health.redis }}</el-tag>
      </el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="后端服务未连接" />
  </div>
</template>
