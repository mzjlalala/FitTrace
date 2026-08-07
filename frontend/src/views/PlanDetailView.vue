<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiGetMyPlans, apiGetPlan, apiStartPlan, type PlanDetail } from '@/api/plan'

const route = useRoute()
const router = useRouter()

const plan = ref<PlanDetail | null>(null)
const activePlanId = ref<number | null>(null)
const starting = ref(false)
const loading = ref(true)

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

const isActive = computed(() => activePlanId.value !== null)

async function loadPlan() {
  loading.value = true
  try {
    const [detailRes, myRes] = await Promise.all([
      apiGetPlan(Number(route.params.id)),
      apiGetMyPlans(),
    ])
    plan.value = detailRes.data
    const mine = myRes.data.find(
      (up) => up.planId === detailRes.data.id && up.status === 'ACTIVE',
    )
    activePlanId.value = mine ? mine.id : null
  } finally {
    loading.value = false
  }
}

async function startPlan() {
  starting.value = true
  try {
    await apiStartPlan(plan.value!.id)
    ElMessage.success('已开始计划，加油！')
    await loadPlan()
  } catch {
    // 拦截器已统一提示（如重复订阅）
  } finally {
    starting.value = false
  }
}

onMounted(loadPlan)
</script>

<template>
  <div v-loading="loading">
    <el-page-header @back="router.back()">
      <template #content>计划详情</template>
    </el-page-header>
    <template v-if="plan">
      <h2>{{ plan.name }}</h2>
      <div class="tags">
        <el-tag type="success">{{ GOAL_LABEL[plan.goal] }}</el-tag>
        <el-tag type="warning">{{ LEVEL_LABEL[plan.level] }}</el-tag>
        <el-tag type="info">{{ plan.frequencyPerWeek }} 次/周</el-tag>
        <el-tag>{{ plan.durationWeeks }} 周</el-tag>
      </div>
      <p class="desc">{{ plan.description }}</p>
      <el-alert
        v-if="isActive"
        title="你正在进行该计划，按周循环完成训练吧"
        type="success"
        :closable="false"
        style="margin-bottom: 12px"
      />
      <el-button v-else type="primary" :loading="starting" @click="startPlan">
        开始计划
      </el-button>

      <el-tabs v-if="plan.weeks.length" class="weeks">
        <el-tab-pane v-for="w in plan.weeks" :key="w.id" :label="`第 ${w.weekNo} 周`">
          <el-row :gutter="16">
            <el-col
              v-for="d in w.days"
              :key="d.id"
              :sm="12"
              :md="8"
              style="margin-bottom: 16px"
            >
              <el-card :class="d.restFlag ? 'rest-day' : ''">
                <template #header>
                  <div class="day-header">
                    <span>Day {{ d.dayNo }}</span>
                    <el-tag v-if="d.restFlag" type="info" size="small">休息</el-tag>
                    <span v-else class="day-title">{{ d.title }}</span>
                  </div>
                </template>
                <el-table
                  v-if="!d.restFlag && d.actions.length"
                  :data="d.actions"
                  size="small"
                >
                  <el-table-column label="动作" min-width="110">
                    <template #default="{ row }">{{ row.action.name }}</template>
                  </el-table-column>
                  <el-table-column label="目标" width="70">
                    <template #default="{ row }">{{ row.sets }} × {{ row.reps }}</template>
                  </el-table-column>
                  <el-table-column label="重量模式" width="90">
                    <template #default="{ row }">
                      {{ row.weightMode === 'FIXED' ? '固定' : row.weightMode }}
                    </template>
                  </el-table-column>
                  <el-table-column label="休息" width="70">
                    <template #default="{ row }">{{ row.restSeconds }}s</template>
                  </el-table-column>
                </el-table>
                <div v-else-if="d.restFlag" class="rest-text">好好休息，恢复体能</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<style scoped>
.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.desc {
  color: #606266;
}
.day-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.day-title {
  font-weight: 600;
}
.rest-day {
  background: #f5f7fa;
}
.rest-text {
  color: #909399;
  text-align: center;
  padding: 12px 0;
}
</style>
