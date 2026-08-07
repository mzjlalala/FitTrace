<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { apiGetProfile, apiUpdateProfile } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  nickname: '',
  gender: '',
  birthDate: '',
  heightCm: null as number | null,
  weightKg: null as number | null,
  goal: '',
  fitnessLevel: '',
  weeklyFrequency: null as number | null,
})

const rules: FormRules = {
  heightCm: [{ type: 'number', min: 50, max: 250, message: '身高 50-250cm', trigger: 'blur' }],
  weightKg: [{ type: 'number', min: 20, max: 300, message: '体重 20-300kg', trigger: 'blur' }],
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
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 640px;
}
</style>
