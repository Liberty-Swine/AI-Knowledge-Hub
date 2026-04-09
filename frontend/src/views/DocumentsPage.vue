<template>
  <el-card>
    <template #header>
      <div style="display:flex; align-items:center; justify-content:space-between;">
        <div style="font-weight:600;">文档管理</div>
        <div style="display:flex; gap:8px; align-items:center;">
          <el-button size="small" @click="load" :disabled="!app.kbId">刷新</el-button>
        </div>
      </div>
    </template>

    <div style="display:flex; gap:12px; align-items:center; margin-bottom: 12px;">
      <el-upload
        :action="uploadUrl"
        :headers="headers"
        :data="{}"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
      >
        <el-button type="primary">上传文档</el-button>
      </el-upload>
      <el-tag type="info">上传后会异步解析/切片/向量化</el-tag>
      <el-switch v-model="autoRefresh" active-text="自动刷新" inactive-text="手动刷新" />
    </div>

    <el-table :data="items" size="small" style="width:100%">
      <el-table-column prop="fileName" label="文件名" />
      <el-table-column prop="id" label="文档ID" width="240" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="chunkCount" label="分片数" width="80" />
      <el-table-column prop="errorMessage" label="错误信息" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="preview(row.id)">预览</el-button>
          <el-button size="small" @click="reindex(row.id)">重建</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { http } from '../api/http'
import type { FileInfo, Result } from '../api/types'
import { useAppStore } from '../store/app'

const auth = useAuthStore()
const app = useAppStore()
const items = ref<FileInfo[]>([])
const autoRefresh = ref(true)
let timer: any = null

const uploadUrl = computed(() => {
  const id = app.kbId.trim()
  return id ? `/api/document/upload?kbId=${encodeURIComponent(id)}` : '/api/document/upload?kbId='
})

const headers = computed(() => ({
  Authorization: auth.token ? `Bearer ${auth.token}` : '',
}))

function beforeUpload() {
  if (!app.kbId.trim()) {
    ElMessage.warning('请先在左侧选择知识库')
    return false
  }
  return true
}

async function load() {
  const id = app.kbId.trim()
  if (!id) {
    return
  }
  const resp = await http.get<Result<FileInfo[]>>(`/api/document?kbId=${encodeURIComponent(id)}`)
  items.value = resp.data.data || []
}

function onUploadSuccess() {
  ElMessage.success('上传成功，正在异步索引')
  load()
}

async function preview(documentId: string) {
  const resp = await http.get<Result<string>>(`/api/document/${documentId}/preview-url`)
  const url = resp.data.data
  if (!url) {
    ElMessage.warning('未获取到预览URL')
    return
  }
  window.open(url, '_blank')
}

async function reindex(documentId: string) {
  await http.post<Result<boolean>>(`/api/document/${documentId}/reindex`)
  ElMessage.success('已提交重建索引')
  await load()
}

async function remove(documentId: string) {
  await ElMessageBox.confirm('确定删除该文档吗？', '确认', { type: 'warning' })
  await http.post<Result<boolean>>(`/api/document/${documentId}/delete`)
  ElMessage.success('已删除')
  await load()
}

function statusLabel(status?: number) {
  if (status === 1) return '索引中'
  if (status === 2) return '就绪'
  if (status === 3) return '失败'
  if (status === 4) return '已删除'
  return '已上传'
}

function statusTagType(status?: number) {
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  if (status === 4) return 'info'
  return 'info'
}

function startAutoRefresh() {
  stopAutoRefresh()
  timer = setInterval(() => {
    if (!autoRefresh.value) return
    // 只有在存在“索引中/失败”时才需要频繁刷新
    const need = items.value.some((x) => x.status === 1)
    if (need) load()
  }, 2500)
}

function stopAutoRefresh() {
  if (timer) clearInterval(timer)
  timer = null
}

watch(
  () => app.kbId,
  async () => {
    items.value = []
    if (app.kbId) await load()
  },
  { immediate: true },
)

onMounted(() => startAutoRefresh())
onUnmounted(() => stopAutoRefresh())
</script>

