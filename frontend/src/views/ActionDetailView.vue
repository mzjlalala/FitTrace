<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiGetAction, MUSCLE_LABEL, type ActionDetail } from '@/api/action'

const route = useRoute()
const router = useRouter()
const detail = ref<ActionDetail | null>(null)
const loading = ref(true)

const DIFFICULTY_LABEL: Record<string, string> = {
  BEGINNER: '初级',
  INTERMEDIATE: '中级',
  ADVANCED: '高级',
}

onMounted(async () => {
  try {
    const res = await apiGetAction(Number(route.params.id))
    detail.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <el-page-header @back="router.back()">
      <template #content>动作详情</template>
    </el-page-header>
    <template v-if="detail">
      <img v-if="detail.coverImage" :src="detail.coverImage" class="detail-cover" alt="封面图" />
      <h2>{{ detail.name }}</h2>
      <div class="tags">
        <el-tag>{{ detail.categoryName }}</el-tag>
        <el-tag type="warning">{{ DIFFICULTY_LABEL[detail.difficulty] }}</el-tag>
        <el-tag type="info">{{ detail.equipment }}</el-tag>
        <el-tag type="success">{{ MUSCLE_LABEL[detail.muscleGroup] || detail.muscleGroup }}</el-tag>
      </div>
      <el-alert
        v-if="detail.description"
        :title="detail.description"
        type="info"
        :closable="false"
        style="margin: 16px 0"
      />
      <section v-if="detail.steps.length">
        <h3>动作步骤</h3>
        <ol class="steps">
          <li v-for="(s, i) in detail.steps" :key="i">{{ s }}</li>
        </ol>
      </section>
      <section v-if="detail.tips.length">
        <h3>技巧提示</h3>
        <el-alert
          v-for="(t, i) in detail.tips"
          :key="i"
          :title="t"
          type="success"
          :closable="false"
          style="margin-bottom: 8px"
        />
      </section>
      <section v-if="detail.cautions.length">
        <h3>注意事项</h3>
        <el-alert
          v-for="(c, i) in detail.cautions"
          :key="i"
          :title="c"
          type="warning"
          :closable="false"
          style="margin-bottom: 8px"
        />
      </section>
    </template>
  </div>
</template>

<style scoped>
.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.steps li {
  margin-bottom: 8px;
  line-height: 1.6;
}
.detail-cover {
  display: block;
  width: 100%;
  max-width: 640px;
  height: auto;
  margin: 0 auto 16px;
  border-radius: 8px;
}
</style>
