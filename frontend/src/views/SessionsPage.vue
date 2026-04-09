<template>
  <el-card>
    <template #header>
      <div style="display:flex; align-items:center; justify-content:space-between;">
        <div style="font-weight:600;">会话管理</div>
        <div style="display:flex; gap:8px; align-items:center;">
          <el-button size="small" @click="load" :disabled="!app.kbId">刷新</el-button>
        </div>
      </div>
    </template>

    <el-form :model="form" inline>
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="可选" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="create" :disabled="!app.kbId">创建会话</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="items" size="small" style="width:100%; margin-top: 12px;">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="id" label="sessionId" width="240" />
      <el-table-column prop="lastActiveTime" label="最后活跃" width="180" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="useSession(row.id)">使用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import type { ChatSession, Result } from '../api/types'
import { useAppStore } from '../store/app'

const app = useAppStore()
const items = ref<ChatSession[]>([])
const form = reactive({ title: '' })

async function load() {
  const id = app.kbId.trim()
  if (!id) {
    return
  }
  const resp = await http.get<Result<ChatSession[]>>(`/api/chat/sessions?kbId=${encodeURIComponent(id)}`)
  items.value = resp.data.data || []
}

async function create() {
  const id = app.kbId.trim()
  if (!id) {
    ElMessage.warning('请先在左侧选择知识库')
    return
  }
  const resp = await http.post<Result<ChatSession>>('/api/chat/sessions', { kbId: id, title: form.title })
  if (resp.data.code === 200) {
    ElMessage.success('创建成功')
    form.title = ''
    await app.loadSessionsByKbId(id)
    await load()
  } else {
    ElMessage.error(resp.data.message || '创建失败')
  }
}

function useSession(sessionId: string) {
  app.setSessionId(sessionId)
  ElMessage.success('已设为当前会话')
}

watch(
  () => app.kbId,
  async () => {
    items.value = []
    if (app.kbId) await load()
  },
  { immediate: true },
)

onMounted(() => {
  if (app.kbId) load()
})
</script>

