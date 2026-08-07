<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  apiGetCategories,
  apiListActions,
  type ActionCategory,
  type ActionListItem,
} from '@/api/action'

const router = useRouter()

const categories = ref<ActionCategory[]>([])
const records = ref<ActionListItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const activeCategory = ref<number | undefined>(undefined)
const difficulty = ref<string | undefined>(undefined)
const keyword = ref('')
const loading = ref(false)

const DIFFICULTY_LABEL: Record<string, string> = {
  BEGINNER: '初级',
  INTERMEDIATE: '中级',
  ADVANCED: '高级',
}

async function loadCategories() {
  const res = await apiGetCategories()
  categories.value = res.data
}

async function loadActions() {
  loading.value = true
  try {
    const res = await apiListActions({
      page: page.value,
      size: size.value,
      categoryId: activeCategory.value,
      difficulty: difficulty.value,
      keyword: keyword.value || undefined,
    })
    records.value = res.data.records
    total.value = res.data.total
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

function onCategorySelect(index: string) {
  activeCategory.value = index === 'all' ? undefined : Number(index)
  page.value = 1
  loadActions()
}

function onSearch() {
  page.value = 1
  loadActions()
}

function goDetail(id: number) {
  router.push(`/actions/${id}`)
}

onMounted(() => {
  loadCategories()
  loadActions()
})
</script>

<template>
  <div class="actions-page">
    <div class="sidebar">
      <el-menu
        :default-active="activeCategory ? String(activeCategory) : 'all'"
        @select="onCategorySelect"
      >
        <el-menu-item index="all">全部</el-menu-item>
        <el-menu-item v-for="c in categories" :key="c.id" :index="String(c.id)">
          {{ c.name }}
        </el-menu-item>
      </el-menu>
    </div>
    <div class="content">
      <div class="toolbar">
        <el-select
          v-model="difficulty"
          placeholder="难度"
          clearable
          style="width: 120px"
          @change="onSearch"
        >
          <el-option label="初级" value="BEGINNER" />
          <el-option label="中级" value="INTERMEDIATE" />
          <el-option label="高级" value="ADVANCED" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索动作名称"
          clearable
          style="width: 220px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        >
          <template #append>
            <el-button @click="onSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div v-loading="loading">
        <el-row :gutter="16">
          <el-col
            v-for="a in records"
            :key="a.id"
            :xs="12"
            :sm="8"
            :md="6"
            style="margin-bottom: 16px"
          >
            <el-card shadow="hover" class="action-card" @click="goDetail(a.id)">
              <div class="cover">
                <img v-if="a.coverImage" :src="a.coverImage" class="cover-img" alt="" />
                <div v-else class="cover-placeholder">{{ a.name.charAt(0) }}</div>
              </div>
              <h3>{{ a.name }}</h3>
              <p class="desc">{{ a.description }}</p>
              <div class="tags">
                <el-tag size="small">{{ a.categoryName }}</el-tag>
                <el-tag size="small" type="warning">{{ DIFFICULTY_LABEL[a.difficulty] }}</el-tag>
                <el-tag size="small" type="info">{{ a.equipment }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!loading && records.length === 0" description="没有符合条件的动作" />
      </div>
      <el-pagination
        v-if="total > size"
        v-model:current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="(s: number) => { size = s; page = 1; loadActions() }"
        @current-change="loadActions"
      />
    </div>
  </div>
</template>

<style scoped>
.actions-page {
  display: flex;
  gap: 16px;
}
.sidebar {
  width: 160px;
  flex-shrink: 0;
}
.content {
  flex: 1;
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.action-card {
  cursor: pointer;
}
.cover {
  margin-bottom: 10px;
}
.cover-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
}
.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 120px;
  border-radius: 4px;
  background: linear-gradient(135deg, var(--el-fill-color-light), var(--el-fill-color));
  color: var(--el-color-primary);
  font-size: 36px;
  font-weight: 600;
}
.action-card h3 {
  margin: 0 0 8px;
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
.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
