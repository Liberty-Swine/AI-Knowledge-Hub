<template>
  <el-container style="height: 100vh">
    <el-aside width="240px" style="border-right: 1px solid #eee; background: #fff">
      <div style="padding: 16px 16px 8px; font-weight: 700; letter-spacing: .2px">AI Knowledge Hub</div>
      <div style="padding: 0 16px 16px;">
        <el-form label-position="top">
          <el-form-item label="当前知识库">
            <el-select
              v-model="app.kbId"
              placeholder="选择知识库"
              filterable
              style="width: 100%"
              :loading="app.loadingKb"
              @change="onKbChange"
            >
              <el-option v-for="kb in app.knowledgeBases" :key="kb.id" :label="kb.name" :value="kb.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="当前会话">
            <el-select
              v-model="app.sessionId"
              placeholder="选择会话"
              filterable
              style="width: 100%"
              :loading="app.loadingSessions"
              :disabled="!app.kbId"
              @change="onSessionChange"
            >
              <el-option
                v-for="s in app.sessions"
                :key="s.id"
                :label="s.title || s.id"
                :value="s.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <el-menu :default-active="active" router>
        <el-menu-item index="/knowledge-bases">知识库</el-menu-item>
        <el-menu-item index="/documents">文档</el-menu-item>
        <el-menu-item index="/sessions">会话</el-menu-item>
        <el-menu-item index="/chat">问答</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex; align-items:center; justify-content: space-between; border-bottom: 1px solid #eee; background:#fff">
        <div style="font-weight: 600">{{ title }}</div>
        <el-button size="small" @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
import { useAppStore } from '../../store/app'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const app = useAppStore()

const active = computed(() => route.path)
const title = computed(() => {
  if (route.path.startsWith('/knowledge-bases')) return '知识库管理'
  if (route.path.startsWith('/documents')) return '文档管理'
  if (route.path.startsWith('/sessions')) return '会话管理'
  if (route.path.startsWith('/chat')) return '智能问答'
  return 'AI Knowledge Hub'
})

function logout() {
  auth.logout()
  router.push('/login')
}

async function onKbChange(v: string) {
  app.setKbId(v)
  await app.loadSessionsByKbId(v)
}

function onSessionChange(v: string) {
  app.setSessionId(v)
}

onMounted(async () => {
  await app.loadKnowledgeBases()
  if (app.kbId) {
    await app.loadSessionsByKbId(app.kbId)
  }
})
</script>

