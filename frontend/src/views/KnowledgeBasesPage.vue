<template>
  <el-row :gutter="12">
    <el-col :span="10">
      <el-card>
        <template #header>
          <div style="display:flex; align-items:center; justify-content:space-between;">
            <div style="font-weight:600;">知识库列表</div>
            <el-button size="small" @click="load">刷新</el-button>
          </div>
        </template>
        <el-table :data="items" size="small" style="width: 100%">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="id" label="ID" width="240" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="small" @click="useKb(row.id)">使用</el-button>
              <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card>
        <template #header>
          <div style="font-weight:600;">创建知识库</div>
        </template>
        <el-form :model="form" label-width="90px">
          <el-form-item label="名称">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="create">创建</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'
import type { KnowledgeBase, Result } from '../api/types'
import { useAppStore } from '../store/app'

const items = ref<KnowledgeBase[]>([])
const saving = ref(false)
const form = reactive({ name: '', description: '' })
const app = useAppStore()

async function load() {
  const resp = await http.get<Result<KnowledgeBase[]>>('/api/knowledge-bases')
  items.value = resp.data.data || []
  app.knowledgeBases = items.value
}

async function create() {
  saving.value = true
  try {
    const resp = await http.post<Result<KnowledgeBase>>('/api/knowledge-bases', form)
    if (resp.data.code !== 200) {
      ElMessage.error(resp.data.message || '创建失败')
      return
    }
    ElMessage.success('创建成功')
    form.name = ''
    form.description = ''
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(id: string) {
  await ElMessageBox.confirm('确定删除该知识库吗？', '确认', { type: 'warning' })
  const resp = await http.delete<Result<boolean>>(`/api/knowledge-bases/${id}`)
  if (resp.data.code === 200) {
    ElMessage.success('已删除')
    await load()
  } else {
    ElMessage.error(resp.data.message || '删除失败')
  }
}

function useKb(kbId: string) {
  app.setKbId(kbId)
  app.loadSessionsByKbId(kbId)
  ElMessage.success('已设为当前知识库')
}

onMounted(load)
</script>

