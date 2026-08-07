<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiAdminListUsers, apiAdminUpdateUserStatus, type AdminUser } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const records = ref<AdminUser[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const keyword = ref('')

const ROLE_LABEL: Record<string, string> = {
  ADMIN: '管理员',
  USER: '普通用户',
}

async function load() {
  loading.value = true
  try {
    const res = await apiAdminListUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    records.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row: AdminUser) {
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await apiAdminUpdateUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success(`已${action}`)
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="toolbar">
      <h2>用户管理</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable style="width: 220px; margin-right: 12px" @keyup.enter="page = 1; load()" @clear="page = 1; load()" />
        <el-button type="primary" @click="page = 1; load()">搜索</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">{{ ROLE_LABEL[row.role] || row.role }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">正常</el-tag>
          <el-tag v-else type="danger" size="small">已禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            v-if="row.id !== auth.user?.id"
            link
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
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
</style>
