<template>
  <el-row :gutter="12">
    <el-col :span="8">
      <el-card>
        <template #header>
          <div style="font-weight:600;">问答参数</div>
        </template>
        <el-form :model="form" label-width="90px">
          <el-form-item label="kbId">
            <el-input v-model="form.kbId" placeholder="知识库ID" readonly />
          </el-form-item>
          <el-form-item label="sessionId">
            <el-input v-model="form.sessionId" placeholder="会话ID" readonly />
          </el-form-item>
          <el-form-item label="topK">
            <el-input-number v-model="form.topK" :min="1" :max="40" />
          </el-form-item>
          <el-form-item label="问题">
            <el-input v-model="form.question" type="textarea" rows="4" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="ask">提问</el-button>
            <el-button :disabled="!form.sessionId" @click="loadHistory">刷新历史</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>

    <el-col :span="16">
      <el-card>
        <template #header>
          <div style="font-weight:600;">回答</div>
        </template>
        <el-input v-model="answer" type="textarea" :rows="10" readonly />

        <div style="margin-top: 12px; font-weight: 600;">引用（citations）</div>
        <el-table :data="citations" size="small" style="width:100%; margin-top: 8px;">
          <el-table-column prop="fileName" label="文件名" />
          <el-table-column prop="documentId" label="documentId" width="220" />
          <el-table-column prop="chunkIndex" label="chunk" width="70" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="open(row.previewUrl)" :disabled="!row.previewUrl">预览</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card style="margin-top: 12px;">
        <template #header>
          <div style="font-weight:600;">会话历史</div>
        </template>
        <el-empty v-if="history.length === 0" description="暂无历史记录" />
        <el-timeline v-else>
          <el-timeline-item v-for="h in history" :key="h.id" :timestamp="h.createTime">
            <div style="font-weight:600;">Q：{{ h.userQuestion }}</div>
            <div style="margin-top:6px; white-space: pre-wrap;">A：{{ h.aiAnswer }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import type { ChatAskResponse, Result } from '../api/types'
import { useAppStore } from '../store/app'

const loading = ref(false)
const answer = ref('')
const citations = ref<any[]>([])
const history = ref<any[]>([])
const app = useAppStore()

const form = reactive({
  kbId: '',
  sessionId: '',
  question: '',
  topK: 10,
})

async function ask() {
  if (!form.kbId.trim() || !form.sessionId.trim() || !form.question.trim()) {
    ElMessage.warning('请先在左侧选择知识库与会话，并输入问题')
    return
  }
  loading.value = true
  try {
    const resp = await http.post<Result<ChatAskResponse>>('/api/chat/ask', {
      kbId: form.kbId,
      sessionId: form.sessionId,
      question: form.question,
      topK: form.topK,
    })
    if (resp.data.code !== 200) {
      ElMessage.error(resp.data.message || '调用失败')
      return
    }
    answer.value = resp.data.data.answer
    citations.value = resp.data.data.citations || []
    await loadHistory()
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  if (!form.sessionId) return
  const resp = await http.get<Result<any[]>>(`/api/chat/sessions/${encodeURIComponent(form.sessionId)}/messages`)
  history.value = resp.data.data || []
}

function open(url?: string) {
  if (!url) return
  window.open(url, '_blank')
}

watch(
  () => [app.kbId, app.sessionId],
  async ([kbId, sessionId]) => {
    form.kbId = kbId || ''
    form.sessionId = sessionId || ''
    answer.value = ''
    citations.value = []
    history.value = []
    if (sessionId) await loadHistory()
  },
  { immediate: true },
)
</script>

