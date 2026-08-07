<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiListPlans, apiRecommendPlans, type PlanVO } from '@/api/plan'

const router = useRouter()

const recommended = ref<PlanVO[]>([])
const plans = ref<PlanVO[]>([])

const GOAL_LABEL: Record<string, string> = {
  MUSCLE_GAIN: '增肌',
  LOSE_FAT: '减脂',
  KEEP_FIT: '塑形',
  STRENGTH: '力量',
}
const LEVEL_LABEL: Record<string, string> = {
  BEGINNER: '初级',
  INTERMEDIATE: '中级',
  ADVANCED: '高级',
}

function goDetail(id: number) {
  router.push(`/plans/${id}`)
}

onMounted(async () => {
  try {
    const [recRes, planRes] = await Promise.all([apiRecommendPlans(), apiListPlans()])
    recommended.value = recRes.data
    plans.value = planRes.data
  } catch {
    // 拦截器已统一提示
  }
})
</script>

<template>
  <div>
    <h2>计划中心</h2>
    <template v-if="recommended.length">
      <h3>为你推荐</h3>
      <el-row :gutter="16">
        <el-col
          v-for="p in recommended"
          :key="'r' + p.id"
          :xs="12"
          :sm="8"
          :md="6"
          style="margin-bottom: 16px"
        >
          <el-card shadow="hover" class="plan-card" @click="goDetail(p.id)">
            <h4>{{ p.name }}</h4>
            <div class="tags">
              <el-tag size="small" type="success">{{ GOAL_LABEL[p.goal] }}</el-tag>
              <el-tag size="small" type="warning">{{ LEVEL_LABEL[p.level] }}</el-tag>
              <el-tag size="small" type="info">{{ p.frequencyPerWeek }} 次/周</el-tag>
              <el-tag size="small">{{ p.durationWeeks }} 周</el-tag>
            </div>
            <p class="desc">{{ p.description }}</p>
          </el-card>
        </el-col>
      </el-row>
    </template>
    <h3>全部计划</h3>
    <el-row :gutter="16">
      <el-col
        v-for="p in plans"
        :key="'p' + p.id"
        :xs="12"
        :sm="8"
        :md="6"
        style="margin-bottom: 16px"
      >
        <el-card shadow="hover" class="plan-card" @click="goDetail(p.id)">
          <h4>{{ p.name }}</h4>
          <div class="tags">
            <el-tag size="small" type="success">{{ GOAL_LABEL[p.goal] }}</el-tag>
            <el-tag size="small" type="warning">{{ LEVEL_LABEL[p.level] }}</el-tag>
            <el-tag size="small" type="info">{{ p.frequencyPerWeek }} 次/周</el-tag>
            <el-tag size="small">{{ p.durationWeeks }} 周</el-tag>
          </div>
          <p class="desc">{{ p.description }}</p>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="plans.length === 0" description="暂无计划" />
  </div>
</template>

<style scoped>
.plan-card {
  cursor: pointer;
}
.plan-card h4 {
  margin: 0 0 8px;
}
.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.desc {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
