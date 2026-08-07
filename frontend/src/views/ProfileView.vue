<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import * as echarts from 'echarts/core'
import { HeatmapChart } from 'echarts/charts'
import { CalendarComponent, TooltipComponent, VisualMapComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { apiGetProfile, apiUpdateProfile } from '@/api/auth'
import { apiUploadImage } from '@/api/oss'
import { apiGetStatsHeatmap, apiGetStatsSummary, type HeatmapDay, type TrainingSummary } from '@/api/training'
import { useAuthStore } from '@/stores/auth'
import type { UploadRequestOptions } from 'element-plus'

echarts.use([HeatmapChart, CalendarComponent, TooltipComponent, VisualMapComponent, CanvasRenderer])

const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const summary = ref<TrainingSummary | null>(null)
const heatmap = ref<HeatmapDay[]>([])
const heatmapRef = ref<HTMLDivElement>()
let chart: ECharts | null = null

const form = reactive({
  nickname: '',
  avatar: '',
  gender: '',
  birthDate: '',
  heightCm: null as number | null,
  weightKg: null as number | null,
  goal: '',
  fitnessLevel: '',
  weeklyFrequency: null as number | null,
})

/** 上传头像到 OSS，成功后将 URL 写入表单（点「保存」后生效） */
async function uploadAvatar(file: File) {
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  const res = await apiUploadImage(file)
  form.avatar = res.data
  ElMessage.success('头像上传成功，点击保存后生效')
}

const rules: FormRules = {
  heightCm: [{ type: 'number', min: 50, max: 250, message: '身高 50-250cm', trigger: 'blur' }],
  weightKg: [{ type: 'number', min: 20, max: 300, message: '体重 20-300kg', trigger: 'blur' }],
}

async function loadStats() {
  const [s, h] = await Promise.all([apiGetStatsSummary(), apiGetStatsHeatmap()])
  summary.value = s.data
  heatmap.value = h.data
}

function renderHeatmap() {
  if (!heatmapRef.value || heatmap.value.length === 0) return
  chart = echarts.init(heatmapRef.value)
  chart.setOption({
    tooltip: {},
    visualMap: {
      min: 0,
      max: 4,
      calculable: false,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127'] },
    },
    calendar: {
      range: [heatmap.value[0]?.date ?? '', heatmap.value[heatmap.value.length - 1]?.date ?? ''],
      top: 40,
      left: 40,
      right: 20,
      cellSize: ['auto', 14],
      itemStyle: { borderWidth: 0.5, borderColor: '#fff' },
      splitLine: { show: false },
      yearLabel: { show: true },
      monthLabel: { show: true },
      dayLabel: { show: false },
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: heatmap.value.map((d) => [d.date, d.count]),
        emphasis: { itemStyle: { borderColor: '#333', borderWidth: 1 } },
      },
    ],
  })
  window.addEventListener('resize', onResize)
}

function onResize() {
  chart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await apiGetProfile()
    Object.assign(form, res.data)
    auth.user = res.data
  } finally {
    loading.value = false
  }
  await loadStats()
  await nextTick()
  renderHeatmap()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
})

async function onSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    const res = await apiUpdateProfile({ ...form })
    Object.assign(form, res.data)
    auth.user = res.data
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <el-card v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="头像">
          <el-upload
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp,image/gif"
            :http-request="(options: UploadRequestOptions) => uploadAvatar(options.file)"
          >
            <el-avatar :size="72" :src="form.avatar || undefined" class="avatar-uploader">
              {{ (form.nickname || auth.user?.username || 'U').charAt(0) }}
            </el-avatar>
          </el-upload>
          <span class="avatar-tip">点击头像上传（jpg/png/webp/gif，≤5MB）</span>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input :model-value="auth.user?.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" clearable placeholder="选择性别">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="身高(cm)" prop="heightCm">
          <el-input-number v-model="form.heightCm" :min="50" :max="250" :step="0.5" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weightKg">
          <el-input-number v-model="form.weightKg" :min="20" :max="300" :step="0.1" />
        </el-form-item>
        <el-form-item label="训练目标">
          <el-select v-model="form.goal" clearable placeholder="选择目标">
            <el-option label="减脂" value="LOSE_FAT" />
            <el-option label="增肌" value="MUSCLE_GAIN" />
            <el-option label="保持健康" value="KEEP_FIT" />
            <el-option label="提升力量" value="STRENGTH" />
          </el-select>
        </el-form-item>
        <el-form-item label="健身水平">
          <el-select v-model="form.fitnessLevel" clearable placeholder="选择水平">
            <el-option label="新手" value="BEGINNER" />
            <el-option label="中级" value="INTERMEDIATE" />
            <el-option label="高级" value="ADVANCED" />
          </el-select>
        </el-form-item>
        <el-form-item label="周训练频次">
          <el-input-number v-model="form.weeklyFrequency" :min="0" :max="7" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="summary" class="stats-card">
      <template #header>训练统计</template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ summary.totalCount }}</div>
            <div class="stat-label">总训练次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ (summary.totalMinutes / 60).toFixed(1) }}</div>
            <div class="stat-label">总时长（小时）</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ summary.checkInDays }}</div>
            <div class="stat-label">打卡天数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ summary.streakDays }}</div>
            <div class="stat-label">连续打卡（天）</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card v-if="summary && summary.prList.length > 0" class="stats-card">
      <template #header>个人纪录（PR）</template>
      <el-table :data="summary.prList" size="small">
        <el-table-column prop="actionName" label="动作" min-width="160" />
        <el-table-column label="重量" width="110">
          <template #default="{ row }">{{ row.weightKg }} kg</template>
        </el-table-column>
        <el-table-column label="次数" width="80">
          <template #default="{ row }">{{ row.reps ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="recordDate" label="日期" width="120" />
      </el-table>
    </el-card>

    <el-card v-if="heatmap.length > 0" class="stats-card">
      <template #header>训练热力图（近 365 天）</template>
      <div ref="heatmapRef" class="heatmap"></div>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 900px;
}
.stats-card {
  margin-top: 16px;
}
.stat-item {
  text-align: center;
  padding: 8px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
}
.stat-label {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.heatmap {
  width: 100%;
  height: 220px;
}
.avatar-uploader {
  cursor: pointer;
  font-size: 28px;
  background: var(--el-color-primary);
}
.avatar-tip {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
