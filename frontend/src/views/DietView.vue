<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  apiCreateDietRecord,
  apiDeleteDietRecord,
  apiGetDietSummary,
  apiListDietRecords,
  apiListFoods,
  type DietFood,
  type DietRecord,
  type DietSummary,
} from '@/api/diet'

const date = ref(new Date().toISOString().slice(0, 10))
const records = ref<DietRecord[]>([])
const summary = ref<DietRecord | null>(null)
const weekSummary = ref<DietSummary[]>([])
const loading = ref(false)
const submitting = ref(false)

const mealType = ref('LUNCH')
const foodOptions = ref<DietFood[]>([])
const foodId = ref<number | null>(null)
const quantityG = ref(100)

const MEAL_LABEL: Record<string, string> = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐',
  SNACK: '加餐',
}

function calcTotal(records: DietRecord[]): DietRecord {
  const sum = (key: 'caloriesKcal' | 'proteinG' | 'fatG' | 'carbG') =>
    Math.round(records.reduce((acc, r) => acc + (r[key] ?? 0), 0) * 10) / 10
  return {
    id: 0,
    recordDate: date.value,
    mealType: '',
    foodId: 0,
    foodName: '',
    category: '',
    quantityG: 0,
    caloriesKcal: sum('caloriesKcal'),
    proteinG: sum('proteinG'),
    fatG: sum('fatG'),
    carbG: sum('carbG'),
  }
}

async function loadDay() {
  loading.value = true
  try {
    const res = await apiListDietRecords(date.value)
    records.value = res.data
    summary.value = calcTotal(res.data)
  } finally {
    loading.value = false
  }
}

async function loadWeek() {
  const end = date.value
  const start = new Date(new Date(end).getTime() - 6 * 86400000).toISOString().slice(0, 10)
  const res = await apiGetDietSummary(start, end)
  weekSummary.value = res.data
}

async function onSearchFood(keyword: string) {
  const res = await apiListFoods({ page: 1, size: 20, keyword })
  foodOptions.value = res.data.records
}

async function addRecord() {
  if (!foodId.value) {
    ElMessage.warning('请选择食物')
    return
  }
  submitting.value = true
  try {
    await apiCreateDietRecord({
      recordDate: date.value,
      mealType: mealType.value,
      foodId: foodId.value,
      quantityG: quantityG.value,
    })
    ElMessage.success('已记录')
    foodId.value = null
    await loadDay()
    await loadWeek()
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  await ElMessageBox.confirm('确定删除这条饮食记录吗？', '提示', { type: 'warning' })
  await apiDeleteDietRecord(id)
  ElMessage.success('已删除')
  await loadDay()
  await loadWeek()
}

watch(date, () => {
  loadDay()
  loadWeek()
})

onMounted(() => {
  loadDay()
  loadWeek()
  onSearchFood('')
})
</script>

<template>
  <div class="diet-page">
    <div class="toolbar">
      <h2>饮食记录</h2>
      <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" />
    </div>

    <el-card class="summary-card" v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ summary?.caloriesKcal ?? 0 }}</div>
            <div class="stat-label">热量（千卡）</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ summary?.proteinG ?? 0 }}</div>
            <div class="stat-label">蛋白质（g）</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ summary?.fatG ?? 0 }}</div>
            <div class="stat-label">脂肪（g）</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ summary?.carbG ?? 0 }}</div>
            <div class="stat-label">碳水（g）</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="add-card">
      <div class="add-row">
        <el-select v-model="mealType" style="width: 110px">
          <el-option v-for="(label, value) in MEAL_LABEL" :key="value" :label="label" :value="value" />
        </el-select>
        <el-select
          v-model="foodId"
          filterable
          remote
          clearable
          placeholder="搜索食物（如：米饭）"
          style="width: 260px"
          :remote-method="onSearchFood"
          :loading="false"
        >
          <el-option v-for="f in foodOptions" :key="f.id" :label="`${f.name}（${f.caloriesPer100g} kcal/100g）`" :value="f.id" />
        </el-select>
        <el-input-number v-model="quantityG" :min="1" :max="5000" />
        <span class="unit">克</span>
        <el-button type="primary" :loading="submitting" @click="addRecord">添加</el-button>
      </div>
    </el-card>

    <el-table :data="records" v-loading="loading" empty-text="这一天还没有饮食记录">
      <el-table-column label="餐次" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ MEAL_LABEL[row.mealType] || row.mealType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="foodName" label="食物" min-width="140" />
      <el-table-column label="分类" width="90">
        <template #default="{ row }">{{ row.category }}</template>
      </el-table-column>
      <el-table-column label="食用量" width="90">
        <template #default="{ row }">{{ row.quantityG }}g</template>
      </el-table-column>
      <el-table-column label="热量" width="90">
        <template #default="{ row }">{{ row.caloriesKcal }} kcal</template>
      </el-table-column>
      <el-table-column label="蛋白/脂肪/碳水" min-width="180">
        <template #default="{ row }">
          {{ row.proteinG }} / {{ row.fatG }} / {{ row.carbG }} g
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-card class="week-card">
      <template #header>近 7 天热量（千卡）</template>
      <div v-for="d in weekSummary" :key="d.date" class="week-row">
        <span class="week-date">{{ d.date }}</span>
        <el-progress
          :percentage="Math.min(100, Math.round((d.caloriesKcal / 2500) * 100))"
          :stroke-width="12"
        />
        <span class="week-value">{{ d.caloriesKcal }} kcal</span>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.summary-card {
  margin-bottom: 16px;
}
.stat-item {
  text-align: center;
  padding: 8px 0;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #e6a23c;
}
.stat-label {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}
.add-card {
  margin-bottom: 16px;
}
.add-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.unit {
  color: #909399;
}
.week-card {
  margin-top: 16px;
}
.week-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.week-date {
  width: 100px;
  color: #606266;
  font-size: 13px;
}
.week-value {
  width: 80px;
  text-align: right;
  color: #606266;
  font-size: 13px;
}
</style>
