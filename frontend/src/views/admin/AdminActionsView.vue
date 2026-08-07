<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  apiAdminCreateAction,
  apiAdminDeleteAction,
  apiAdminListActions,
  apiAdminUpdateAction,
  type AdminAction,
} from '@/api/admin'
import { apiGetCategories, type ActionCategory } from '@/api/action'

const records = ref<AdminAction[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const keyword = ref('')
const categories = ref<ActionCategory[]>([])

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)

const DIFFICULTY_LABEL: Record<string, string> = {
  BEGINNER: '初级',
  INTERMEDIATE: '中级',
  ADVANCED: '高级',
}

const MUSCLE_LABEL: Record<string, string> = {
  CHEST: '胸部',
  BACK: '背部',
  LEGS: '腿部',
  SHOULDERS: '肩部',
  ARMS: '手臂',
  CORE: '核心',
  CARDIO: '有氧',
}

interface StepRow {
  key: number
  text: string
}
let stepKey = 0

const form = reactive({
  name: '',
  categoryId: null as number | null,
  muscleGroup: '',
  difficulty: 'BEGINNER',
  equipment: '',
  description: '',
  steps: [] as StepRow[],
  tips: [] as StepRow[],
  cautions: [] as StepRow[],
  status: 1,
})

async function load() {
  loading.value = true
  try {
    const res = await apiAdminListActions({ page: page.value, size, keyword: keyword.value || undefined })
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
    categoryId: null,
    muscleGroup: '',
    difficulty: 'BEGINNER',
    equipment: '',
    description: '',
    steps: [],
    tips: [],
    cautions: [],
    status: 1,
  })
  addStep(form.steps)
  addStep(form.tips)
  addStep(form.cautions)
  dialogVisible.value = true
}

function openEdit(row: AdminAction) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    categoryId: row.categoryId,
    muscleGroup: row.muscleGroup ?? '',
    difficulty: row.difficulty ?? 'BEGINNER',
    equipment: row.equipment ?? '',
    description: row.description ?? '',
    steps: (row.steps ?? []).map((t) => ({ key: ++stepKey, text: t })),
    tips: (row.tips ?? []).map((t) => ({ key: ++stepKey, text: t })),
    cautions: (row.cautions ?? []).map((t) => ({ key: ++stepKey, text: t })),
    status: row.status,
  })
  dialogVisible.value = true
}

function addStep(list: StepRow[]) {
  list.push({ key: ++stepKey, text: '' })
}

function texts(list: StepRow[]) {
  return list.map((s) => s.text).filter((t) => t.trim() !== '')
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入动作名称')
    return
  }
  submitting.value = true
  try {
    const payload = {
      name: form.name.trim(),
      categoryId: form.categoryId,
      muscleGroup: form.muscleGroup || null,
      difficulty: form.difficulty || null,
      equipment: form.equipment || null,
      coverImage: null,
      videoUrl: null,
      description: form.description || null,
      steps: texts(form.steps),
      tips: texts(form.tips),
      cautions: texts(form.cautions),
      status: form.status,
    }
    if (editingId.value) {
      await apiAdminUpdateAction(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await apiAdminCreateAction(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row: AdminAction) {
  const action = row.status === 1 ? '下架' : '上架'
  await ElMessageBox.confirm(`确定${action}「${row.name}」吗？`, '提示', { type: 'warning' })
  await apiAdminDeleteAction(row.id)
  // 重新上架通过编辑接口把 status 改回 1
  if (row.status === 1) {
    ElMessage.success('已下架')
  } else {
    await apiAdminUpdateAction(row.id, {
      ...row,
      status: 1,
      categoryId: row.categoryId,
      name: row.name,
      steps: row.steps ?? [],
      tips: row.tips ?? [],
      cautions: row.cautions ?? [],
    })
    ElMessage.success('已上架')
  }
  load()
}

onMounted(async () => {
  load()
  const res = await apiGetCategories()
  categories.value = res.data
})
</script>

<template>
  <div class="admin-page">
    <div class="toolbar">
      <h2>动作管理</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索动作名" clearable style="width: 200px; margin-right: 12px" @keyup.enter="page = 1; load()" @clear="page = 1; load()" />
        <el-button type="primary" @click="page = 1; load()">搜索</el-button>
        <el-button type="success" @click="openCreate">新建动作</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="categoryName" label="分类" width="90" />
      <el-table-column label="肌群" width="90">
        <template #default="{ row }">{{ MUSCLE_LABEL[row.muscleGroup] || row.muscleGroup || '—' }}</template>
      </el-table-column>
      <el-table-column label="难度" width="90">
        <template #default="{ row }">{{ DIFFICULTY_LABEL[row.difficulty] || row.difficulty }}</template>
      </el-table-column>
      <el-table-column prop="equipment" label="器械" width="90" />
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
      layout="prev, pager, next, total"
      :total="total"
      :page-size="size"
      :current-page="page"
      @current-change="(p: number) => { page = p; load() }"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑动作' : '新建动作'" width="640px">
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" clearable placeholder="选择分类">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="肌群">
              <el-select v-model="form.muscleGroup" clearable placeholder="选择肌群">
                <el-option v-for="(label, value) in MUSCLE_LABEL" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty">
                <el-option label="初级" value="BEGINNER" />
                <el-option label="中级" value="INTERMEDIATE" />
                <el-option label="高级" value="ADVANCED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="器械">
              <el-input v-model="form.equipment" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="步骤">
          <div v-for="(s, i) in form.steps" :key="s.key" class="list-row">
            <el-input v-model="s.text" placeholder="步骤内容" />
            <el-button link type="danger" @click="form.steps.splice(i, 1)">删</el-button>
          </div>
          <el-button size="small" @click="addStep(form.steps)">+ 添加步骤</el-button>
        </el-form-item>
        <el-form-item label="技巧">
          <div v-for="(s, i) in form.tips" :key="s.key" class="list-row">
            <el-input v-model="s.text" placeholder="技巧内容" />
            <el-button link type="danger" @click="form.tips.splice(i, 1)">删</el-button>
          </div>
          <el-button size="small" @click="addStep(form.tips)">+ 添加技巧</el-button>
        </el-form-item>
        <el-form-item label="注意">
          <div v-for="(s, i) in form.cautions" :key="s.key" class="list-row">
            <el-input v-model="s.text" placeholder="注意事项" />
            <el-button link type="danger" @click="form.cautions.splice(i, 1)">删</el-button>
          </div>
          <el-button size="small" @click="addStep(form.cautions)">+ 添加注意</el-button>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
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
.list-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  width: 100%;
}
</style>
