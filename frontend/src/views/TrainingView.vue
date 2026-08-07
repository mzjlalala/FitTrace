<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  apiCreateTrainingRecord,
  apiDeleteTrainingRecord,
  apiGetTrainingRecord,
  apiListTrainingRecords,
  apiUpdateTrainingRecord,
  type TrainingRecordDetail,
  type TrainingRecordVO,
  type TrainingSetInput,
} from '@/api/training'
import { apiListActions, MUSCLE_LABEL, type ActionListItem } from '@/api/action'

const records = ref<TrainingRecordVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

/** 日期范围筛选（默认当月：月初 ~ 今天） */
function fmt(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
const todayStr = fmt(new Date())
const dateRange = ref<[string, string]>([fmt(new Date(new Date().getFullYear(), new Date().getMonth(), 1)), todayStr])

const dialogVisible = ref(false)
const drawerVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const detail = ref<TrainingRecordDetail | null>(null)

const actionOptions = ref<ActionListItem[]>([])
const activeMuscle = ref<string | undefined>(undefined)

/**
 * 弹窗内动作下拉的可选项：按肌群过滤；已选动作始终保留，
 * 避免切换肌群筛选后当前行已选动作在下拉中消失
 */
const visibleActions = computed(() => {
  const filtered = activeMuscle.value
    ? actionOptions.value.filter((a) => a.muscleGroup === activeMuscle.value)
    : actionOptions.value
  const selectedIds = new Set(form.setRows.map((r) => r.actionId).filter((id): id is number => !!id))
  const extra = actionOptions.value.filter((a) => selectedIds.has(a.id) && !filtered.some((f) => f.id === a.id))
  return filtered.concat(extra)
})

const FEEL_LABEL: Record<string, string> = {
  GOOD: '状态好',
  NORMAL: '一般',
  TIRED: '疲劳',
}

interface SetRow extends TrainingSetInput {
  key: number
}

const form = reactive({
  trainingDate: new Date().toISOString().slice(0, 10),
  durationMinutes: 60 as number | null,
  feel: 'GOOD' as string | null,
  note: '' as string | null,
  setRows: [] as SetRow[],
})

let setKey = 0

const dialogTitle = computed(() => (editingId.value ? '编辑训练记录' : '记录训练'))

async function loadRecords() {
  loading.value = true
  try {
    const res = await apiListTrainingRecords(page.value, size.value, dateRange.value?.[0], dateRange.value?.[1])
    records.value = res.data.records
    total.value = res.data.total
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

function onRangeChange() {
  page.value = 1
  loadRecords()
}

function addSetRow() {
  form.setRows.push({
    key: ++setKey,
    actionId: actionOptions.value[0]?.id ?? 0,
    weightKg: null,
    reps: 10,
    doneFlag: true,
  })
}

async function openCreate() {
  editingId.value = null
  form.trainingDate = new Date().toISOString().slice(0, 10)
  form.durationMinutes = 60
  form.feel = 'GOOD'
  form.note = ''
  form.setRows = []
  addSetRow()
  dialogVisible.value = true
}

async function openEdit(id: number) {
  const res = await apiGetTrainingRecord(id)
  const d = res.data
  editingId.value = id
  form.trainingDate = d.trainingDate
  form.durationMinutes = d.durationMinutes
  form.feel = d.feel
  form.note = d.note
  form.setRows = d.sets.map((s) => ({
    key: ++setKey,
    actionId: s.actionId,
    weightKg: s.weightKg,
    reps: s.reps,
    doneFlag: s.doneFlag,
  }))
  dialogVisible.value = true
}

async function openDetail(id: number) {
  const res = await apiGetTrainingRecord(id)
  detail.value = res.data
  drawerVisible.value = true
}

async function submit() {
  if (!form.trainingDate) {
    ElMessage.warning('请选择训练日期')
    return
  }
  if (form.setRows.length === 0 || form.setRows.some((r) => !r.actionId)) {
    ElMessage.warning('请至少添加一组有效的动作')
    return
  }
  submitting.value = true
  try {
    const payload = {
      trainingDate: form.trainingDate,
      durationMinutes: form.durationMinutes,
      feel: form.feel,
      note: form.note || null,
      planId: null,
      sets: form.setRows.map(({ actionId, weightKg, reps, doneFlag }) => ({
        actionId,
        weightKg,
        reps,
        doneFlag,
      })),
    }
    if (editingId.value) {
      await apiUpdateTrainingRecord(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await apiCreateTrainingRecord(payload)
      ElMessage.success('已记录')
    }
    dialogVisible.value = false
    loadRecords()
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  await ElMessageBox.confirm('确定删除这条训练记录吗？', '提示', { type: 'warning' })
  await apiDeleteTrainingRecord(id)
  ElMessage.success('已删除')
  if (records.value.length === 1 && page.value > 1) {
    page.value -= 1
  }
  loadRecords()
}

function actionName(id: number) {
  return actionOptions.value.find((a) => a.id === id)?.name ?? ''
}

onMounted(async () => {
  loadRecords()
  const res = await apiListActions({ page: 1, size: 100 })
  actionOptions.value = res.data.records
})
</script>

<template>
  <div class="training-page">
    <div class="toolbar">
      <h2>训练记录</h2>
      <div class="toolbar-right">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
          @change="onRangeChange"
        />
        <el-button type="primary" @click="openCreate">记录训练</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="records" empty-text="还没有训练记录，点击右上角开始">
      <el-table-column prop="trainingDate" label="日期" width="120" />
      <el-table-column label="计划" width="160">
        <template #default="{ row }">{{ row.planName || '自由训练' }}</template>
      </el-table-column>
      <el-table-column label="时长" width="100">
        <template #default="{ row }">{{ row.durationMinutes ? row.durationMinutes + ' 分钟' : '—' }}</template>
      </el-table-column>
      <el-table-column label="感受" width="100">
        <template #default="{ row }">{{ FEEL_LABEL[row.feel] || '—' }}</template>
      </el-table-column>
      <el-table-column prop="note" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
          <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
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
      @size-change="(s: number) => { size = s; page = 1; loadRecords() }"
      @current-change="(p: number) => { page = p; loadRecords() }"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="训练日期">
              <el-date-picker v-model="form.trainingDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长(分钟)">
              <el-input-number v-model="form.durationMinutes" :min="1" :max="600" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="训练感受">
          <el-select v-model="form.feel" placeholder="选择感受">
            <el-option label="状态好" value="GOOD" />
            <el-option label="一般" value="NORMAL" />
            <el-option label="疲劳" value="TIRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>

        <el-form-item label="训练组">
          <div class="set-toolbar">
            <el-select
              v-model="activeMuscle"
              clearable
              placeholder="按肌群筛选动作"
              style="width: 200px"
              @clear="activeMuscle = undefined"
            >
              <el-option v-for="(label, value) in MUSCLE_LABEL" :key="value" :label="label" :value="value" />
            </el-select>
          </div>
          <el-table :data="form.setRows" size="small">
            <el-table-column label="动作" min-width="200">
              <template #default="{ row }">
                <el-select v-model="row.actionId" filterable placeholder="选择动作">
                  <el-option v-for="a in visibleActions" :key="a.id" :label="a.name" :value="a.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="重量(kg)" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.weightKg" :min="0" :max="500" :controls="false" />
              </template>
            </el-table-column>
            <el-table-column label="次数" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.reps" :min="0" :max="200" :controls="false" />
              </template>
            </el-table-column>
            <el-table-column label="完成" width="80">
              <template #default="{ row }">
                <el-switch v-model="row.doneFlag" />
              </template>
            </el-table-column>
            <el-table-column width="50">
              <template #default="{ $index }">
                <el-button link type="danger" @click="form.setRows.splice($index, 1)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button class="add-set" size="small" @click="addSetRow">+ 添加一组</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" title="训练详情" size="480px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日期">{{ detail.trainingDate }}</el-descriptions-item>
          <el-descriptions-item label="计划">{{ detail.planName || '自由训练' }}</el-descriptions-item>
          <el-descriptions-item label="时长">
            {{ detail.durationMinutes ? detail.durationMinutes + ' 分钟' : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="感受">{{ (detail.feel && FEEL_LABEL[detail.feel]) || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.note || '—' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.sets" size="small" class="detail-sets">
          <el-table-column prop="setNo" label="组" width="60" />
          <el-table-column prop="actionName" label="动作" min-width="160" />
          <el-table-column label="重量" width="90">
            <template #default="{ row }">{{ row.weightKg ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="次数" width="70">
            <template #default="{ row }">{{ row.reps ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="完成" width="70">
            <template #default="{ row }">
              <el-tag v-if="row.doneFlag" type="success" size="small">完成</el-tag>
              <el-tag v-else type="info" size="small">未完成</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.set-toolbar {
  width: 100%;
  margin-bottom: 8px;
}
.add-set {
  margin-top: 8px;
}
.detail-sets {
  margin-top: 16px;
}
</style>
