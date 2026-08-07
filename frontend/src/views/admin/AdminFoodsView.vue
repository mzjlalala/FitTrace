<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  apiAdminCreateFood,
  apiAdminDeleteFood,
  apiAdminListFoods,
  apiAdminUpdateFood,
  type AdminFood,
} from '@/api/admin'
import { apiUploadImage } from '@/api/oss'
import type { UploadRequestOptions } from 'element-plus'

const records = ref<AdminFood[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const keyword = ref('')
const category = ref('')

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)

const CATEGORIES = ['主食', '肉蛋', '蔬菜', '水果', '奶类', '其他']

const form = reactive({
  name: '',
  category: '',
  caloriesPer100g: 0 as number | null,
  proteinPer100g: 0 as number | null,
  fatPer100g: 0 as number | null,
  carbPer100g: 0 as number | null,
  image: '',
  status: 1,
})

/** 上传食物图片到 OSS，成功后将 URL 写入表单 */
async function uploadImage(file: File) {
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  const res = await apiUploadImage(file)
  form.image = res.data
  ElMessage.success('图片上传成功')
}

async function load() {
  loading.value = true
  try {
    const res = await apiAdminListFoods({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
    })
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
    category: '',
    caloriesPer100g: 0,
    proteinPer100g: 0,
    fatPer100g: 0,
    carbPer100g: 0,
    image: '',
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: AdminFood) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    category: row.category ?? '',
    caloriesPer100g: row.caloriesPer100g,
    proteinPer100g: row.proteinPer100g,
    fatPer100g: row.fatPer100g,
    carbPer100g: row.carbPer100g,
    image: row.image ?? '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入食物名称')
    return
  }
  if (form.caloriesPer100g === null) {
    ElMessage.warning('请输入热量')
    return
  }
  submitting.value = true
  try {
    const payload = {
      name: form.name.trim(),
      category: form.category || null,
      caloriesPer100g: form.caloriesPer100g,
      proteinPer100g: form.proteinPer100g ?? 0,
      fatPer100g: form.fatPer100g ?? 0,
      carbPer100g: form.carbPer100g ?? 0,
      image: form.image || null,
      status: form.status,
    }
    if (editingId.value) {
      await apiAdminUpdateFood(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await apiAdminCreateFood(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row: AdminFood) {
  const action = row.status === 1 ? '下架' : '上架'
  await ElMessageBox.confirm(`确定${action}「${row.name}」吗？`, '提示', { type: 'warning' })
  if (row.status === 1) {
    await apiAdminDeleteFood(row.id)
    ElMessage.success('已下架')
  } else {
    await apiAdminUpdateFood(row.id, {
      ...row,
      status: 1,
      name: row.name,
      category: row.category,
      caloriesPer100g: row.caloriesPer100g,
    })
    ElMessage.success('已上架')
  }
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="toolbar">
      <h2>食物管理</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索食物名" clearable style="width: 200px; margin-right: 12px" @keyup.enter="page = 1; load()" @clear="page = 1; load()" />
        <el-select v-model="category" clearable placeholder="分类" style="width: 120px; margin-right: 12px" @change="page = 1; load()">
          <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button type="primary" @click="page = 1; load()">搜索</el-button>
        <el-button type="success" @click="openCreate">新建食物</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img v-if="row.image" :src="row.image" class="food-thumb" alt="" />
          <span v-else class="thumb-empty">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="category" label="分类" width="90" />
      <el-table-column label="热量/100g" width="100">
        <template #default="{ row }">{{ row.caloriesPer100g }} kcal</template>
      </el-table-column>
      <el-table-column label="蛋白/脂肪/碳水" min-width="180">
        <template #default="{ row }">{{ row.proteinPer100g }} / {{ row.fatPer100g }} / {{ row.carbPer100g }} g</template>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑食物' : '新建食物'" width="520px">
      <el-form label-width="110px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" clearable placeholder="选择分类">
            <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp,image/gif"
            :http-request="(options: UploadRequestOptions) => uploadImage(options.file)"
          >
            <div class="image-uploader">
              <img v-if="form.image" :src="form.image" class="image-preview" alt="食物图片" />
              <span v-else class="image-placeholder">点击上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="热量(每100g)" required>
          <el-input-number v-model="form.caloriesPer100g" :min="0" :max="1000" :precision="1" />
          <span class="unit"> kcal</span>
        </el-form-item>
        <el-form-item label="蛋白质(每100g)">
          <el-input-number v-model="form.proteinPer100g" :min="0" :max="100" :precision="1" />
          <span class="unit"> g</span>
        </el-form-item>
        <el-form-item label="脂肪(每100g)">
          <el-input-number v-model="form.fatPer100g" :min="0" :max="100" :precision="1" />
          <span class="unit"> g</span>
        </el-form-item>
        <el-form-item label="碳水(每100g)">
          <el-input-number v-model="form.carbPer100g" :min="0" :max="100" :precision="1" />
          <span class="unit"> g</span>
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
.unit {
  margin-left: 8px;
  color: #909399;
}
.food-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}
.thumb-empty {
  color: #c0c4cc;
}
.image-uploader {
  cursor: pointer;
}
.image-preview {
  width: 96px;
  height: 72px;
  object-fit: cover;
  border-radius: 4px;
}
.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 72px;
  border: 1px dashed #c0c4cc;
  border-radius: 4px;
  background: #fafafa;
  color: #909399;
  font-size: 13px;
}
</style>
