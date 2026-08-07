<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  apiAdminCreatePlan,
  apiAdminDeletePlan,
  apiAdminListPlans,
  apiAdminUpdatePlan,
  type AdminPlan,
} from '@/api/admin'
import { apiGetPlan } from '@/api/plan'
import { apiListActions, type ActionListItem } from '@/api/action'

const records = ref<AdminPlan[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const keyword = ref('')

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const actionOptions = ref<ActionListItem[]>([])

const GOAL_LABEL: Record<string, string> = {
  LOSE_FAT: '减脂',
  MUSCLE_GAIN: '增肌',
  KEEP_FIT: '保持健康',
  STRENGTH: '提升力量',
}
const LEVEL_LABEL: Record<string, string> = {
  BEGINNER: '新手',
  INTERMEDIATE: '中级',
  ADVANCED: '高级',
}
const WEIGHT_MODE_LABEL: Record<string, string> = {
  FIXED: '固定重量',
  PROGRESSIVE: '递增',
}

interface ActionRow {
  key: number
  actionId: number | null
  sets: number | null
  reps: number | null
  weightMode: string
  restSeconds: number | null
}

interface DayForm {
  key: number
  dayNo: number
  restFlag: boolean
  title: string
  actions: ActionRow[]
}

let rowKey = 0
const form = reactive({
  name: '',
  goal: '',
  level: 'BEGINNER',
  durationWeeks: 8 as number | null,
  frequencyPerWeek: 3 as number | null,
  description: '',
  days: [] as DayForm[],
})

function newActionRow(): ActionRow {
  return {
    key: ++rowKey,
    actionId: actionOptions.value[0]?.id ?? null,
    sets: 4,
    reps: 12,
    weightMode: 'FIXED',
    restSeconds: 90,
  }
}

function addDay() {
  form.days.push({
    key: ++rowKey,
    dayNo: form.days.length + 1,
    restFlag: false,
    title: '',
    actions: [newActionRow()],
  })
}

async function load() {
  loading.value = true
  try {
    const res = await apiAdminListPlans({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    records.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    goal: '',
    level: 'BEGINNER',
    durationWeeks: 8,
    frequencyPerWeek: 3,
    description: '',
    days: [],
  })
  addDay()
  dialogVisible.value = true
}

async function openEdit(row: AdminPlan) {
  editingId.value = row.id
  const res = await apiGetPlan(row.id)
  const detail = res.data
  Object.assign(form, {
    name: detail.name,
    goal: detail.goal ?? '',
    level: detail.level ?? 'BEGINNER',
    durationWeeks: detail.durationWeeks,
    frequencyPerWeek: detail.frequencyPerWeek,
    description: detail.description ?? '',
    days: (detail.weeks[0]?.days ?? []).map((d) => ({
      key: ++rowKey,
      dayNo: d.dayNo,
      restFlag: d.restFlag,
      title: d.title ?? '',
      actions: d.actions.map((a) => ({
        key: ++rowKey,
        actionId: a.action.id,
        sets: a.sets,
        reps: a.reps,
        weightMode: a.weightMode ?? 'FIXED',
        restSeconds: a.restSeconds,
      })),
    })),
  })
  if (form.days.length === 0) {
    addDay()
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入计划名称')
    return
  }
  if (form.days.length === 0) {
    ElMessage.warning('请至少添加一个训练日')
    return
  }
  const days = form.days
    .filter((d) => !d.restFlag)
    .map((d) => ({
      dayNo: d.dayNo,
      restFlag: false,
      title: d.title || null,
      actions: d.actions
        .filter((a) => a.actionId)
        .map((a, i) => ({
          actionId: a.actionId!,
          sort: i + 1,
          sets: a.sets,
          reps: a.reps,
          weightMode: a.weightMode,
          restSeconds: a.restSeconds,
        })),
    }))
  const restDays = form.days
    .filter((d) => d.restFlag)
    .map((d) => ({ dayNo: d.dayNo, restFlag: true, title: null, actions: [] }))
  const allDays = [...days, ...restDays].sort((a, b) => a.dayNo - b.dayNo)

  submitting.value = true
  try {
    const payload = {
      name: form.name.trim(),
      goal: form.goal || null,
      level: form.level || null,
      durationWeeks: form.durationWeeks,
      frequencyPerWeek: form.frequencyPerWeek,
      description: form.description || null,
      weeks: [{ weekNo: 1, days: allDays }],
    }
    if (editingId.value) {
      await apiAdminUpdatePlan(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await apiAdminCreatePlan(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row: AdminPlan) {
  const action = row.status === 1 ? '下架' : '上架'
  await ElMessageBox.confirm(`确定${action}计划「${row.name}」吗？`, '提示', { type: 'warning' })
  if (row.status === 1) {
    await apiAdminDeletePlan(row.id)
    ElMessage.success('已下架')
  } else {
    await apiAdminUpdatePlan(row.id, {
      name: row.name,
      goal: row.goal,
      level: row.level,
      durationWeeks: row.durationWeeks,
      frequencyPerWeek: row.frequencyPerWeek,
      description: row.description,
      weeks: [{ weekNo: 1, days: [] }],
    })
    ElMessage.success('已上架')
  }
  load()
}

onMounted(async () => {
  load()
  const res = await apiListActions({ page: 1, size: 100 })
  actionOptions.value = res.data.records
})
</script>

<template>
  <div class="admin-page">
    <div class="toolbar">
      <h2>计划管理</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索计划名" clearable style="width: 200px; margin-right: 12px" @keyup.enter="page = 1; load()" @clear="page = 1; load()" />
        <el-button type="primary" @click="page = 1; load()">搜索</el-button>
        <el-button type="success" @click="openCreate">新建计划</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="目标" width="100">
        <template #default="{ row }">{{ GOAL_LABEL[row.goal] || '—' }}</template>
      </el-table-column>
      <el-table-column label="水平" width="80">
        <template #default="{ row }">{{ LEVEL_LABEL[row.level] || '—' }}</template>
      </el-table-column>
      <el-table-column label="周数/频次" width="110">
        <template #default="{ row }">{{ row.durationWeeks ?? '—' }} 周 / {{ row.frequencyPerWeek ?? '—' }} 次</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">上架</el-tag>
          <el-tag v-else type="info" size="small">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > size"
      class="pager"
      layout="total, sizes, prev, pager, next"
      :total="total"
      :page-size="size"
      :page-sizes="[10, 20, 50]"
      :current-page="page"
      @size-change="(s: number) => { size = s; page = 1; load() }"
      @current-change="(p: number) => { page = p; load() }"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑计划' : '新建计划'" width="860px" top="5vh">
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="目标">
              <el-select v-model="form.goal" clearable placeholder="选择目标">
                <el-option v-for="(label, value) in GOAL_LABEL" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="水平">
              <el-select v-model="form.level">
                <el-option v-for="(label, value) in LEVEL_LABEL" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="总周数">
              <el-input-number v-model="form.durationWeeks" :min="1" :max="52" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每周频次">
              <el-input-number v-model="form.frequencyPerWeek" :min="1" :max="7" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="训练安排">
          <div class="days-wrap">
            <el-card v-for="(day, di) in form.days" :key="day.key" class="day-card" shadow="never">
              <template #header>
                <div class="day-header">
                  <span class="day-title">Day {{ day.dayNo }}</span>
                  <el-switch v-model="day.restFlag" active-text="休息日" />
                  <el-button link type="danger" @click="form.days.splice(di, 1)">删除</el-button>
                </div>
              </template>
              <template v-if="!day.restFlag">
                <el-input v-model="day.title" placeholder="当日主题（如：推日）" style="margin-bottom: 8px" />
                <el-table :data="day.actions" size="small">
                  <el-table-column label="动作" min-width="180">
                    <template #default="{ row }">
                      <el-select v-model="row.actionId" filterable placeholder="选择动作" style="width: 100%">
                        <el-option v-for="a in actionOptions" :key="a.id" :label="a.name" :value="a.id" />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column label="组数" width="90">
                    <template #default="{ row }">
                      <el-input-number v-model="row.sets" :min="1" :max="20" :controls="false" />
                    </template>
                  </el-table-column>
                  <el-table-column label="次数" width="90">
                    <template #default="{ row }">
                      <el-input-number v-model="row.reps" :min="1" :max="100" :controls="false" />
                    </template>
                  </el-table-column>
                  <el-table-column label="重量模式" width="110">
                    <template #default="{ row }">
                      <el-select v-model="row.weightMode">
                        <el-option v-for="(label, value) in WEIGHT_MODE_LABEL" :key="value" :label="label" :value="value" />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column label="休息(s)" width="90">
                    <template #default="{ row }">
                      <el-input-number v-model="row.restSeconds" :min="0" :max="600" :controls="false" />
                    </template>
                  </el-table-column>
                  <el-table-column width="50">
                    <template #default="{ $index }">
                      <el-button link type="danger" @click="day.actions.splice($index, 1)">删</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size="small" style="margin-top: 8px" @click="day.actions.push(newActionRow())">+ 添加动作</el-button>
              </template>
              <el-empty v-else description="休息日" :image-size="40" />
            </el-card>
            <el-button @click="addDay">+ 添加训练日</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.days-wrap {
  width: 100%;
}
.day-card {
  margin-bottom: 12px;
}
.day-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.day-title {
  font-weight: 600;
}
</style>
