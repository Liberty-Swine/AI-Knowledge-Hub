<template>
  <div style="height:100vh; display:flex; align-items:center; justify-content:center;">
    <el-card style="width: 420px">
      <template #header>
        <div style="font-weight: 600">登录</div>
      </template>
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
        </el-form-item>
      </el-form>
      <div style="color:#999; font-size:12px;">
        默认账号：admin / 123456
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'
import type { Result } from '../api/types'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456',
})

async function submit() {
  loading.value = true
  try {
    const resp = await http.post<Result<{ token: string }>>('/api/auth/login', form)
    if (resp.data.code !== 200) {
      ElMessage.error(resp.data.message || '登录失败')
      return
    }
    auth.setToken(resp.data.data.token)
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

