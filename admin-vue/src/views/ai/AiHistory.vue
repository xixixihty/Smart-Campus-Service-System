<template>
  <div class="ai-history-page">
    <div class="page-header">
      <div class="header-left">
        <h2><el-icon><ChatLineRound /></el-icon> AI 对话历史</h2>
        <p class="header-subtitle">查看和管理管理端的AI对话记录</p>
      </div>
      <div class="header-actions">
        <el-button @click="$router.push('/ai-assistant')" type="primary">
          <el-icon><Cpu /></el-icon> 新建对话
        </el-button>
        <el-button @click="loadSessions" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="16" style="flex: 1; min-height: 0">
      <el-col :span="8">
        <el-card shadow="never" class="session-list-card">
          <template #header>
            <span>对话列表</span>
          </template>
          <div v-loading="sessionLoading" class="session-list">
            <div
              v-for="session in sessions"
              :key="session.sessionId"
              :class="['session-item', { active: selectedSessionId === session.sessionId }]"
              @click="selectSession(session)"
            >
              <div class="session-title">{{ session.title || '新对话' }}</div>
              <div class="session-meta">
                <span>{{ session.messageCount }} 条消息</span>
                <span>{{ formatTime(session.lastMessageAt) }}</span>
              </div>
              <el-button
                class="session-delete"
                :icon="Delete"
                circle
                size="small"
                @click.stop="handleDelete(session.sessionId)"
              />
            </div>
            <el-empty v-if="!sessionLoading && sessions.length === 0" description="暂无对话记录" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="chat-card">
          <div class="chat-container">
            <div class="chat-messages" ref="chatMessagesRef" v-if="selectedSessionId">
              <div v-for="(msg, index) in chatHistory" :key="index" :class="['chat-message', msg.role]">
                <div class="chat-avatar">
                  <el-icon v-if="msg.role === 'user'"><User /></el-icon>
                  <el-icon v-else><Cpu /></el-icon>
                </div>
                <div class="chat-bubble" v-html="renderMarkdown(msg.content)"></div>
              </div>
              <div v-if="historyLoading" class="loading-more">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>加载中...</span>
              </div>
            </div>
            <div v-else class="no-session-selected">
              <el-empty description="请选择一个对话记录查看详情" :image-size="100" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <span>确定要删除这个对话记录吗？删除后无法恢复。</span>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">确定删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Cpu, User, Delete, Refresh, Loading, ChatLineRound } from '@element-plus/icons-vue'
import { getAdminAiSessions, getAdminAiChatHistory, deleteAdminAiSession } from '@/api/ai'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

const router = useRouter()
const sessions = ref([])
const sessionLoading = ref(false)
const selectedSessionId = ref(null)
const chatHistory = ref([])
const historyLoading = ref(false)
const chatMessagesRef = ref(null)
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
let pendingDeleteSessionId = null

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  const month = d.getMonth() + 1
  const day = d.getDate()
  return month + '月' + day + '日'
}

function renderMarkdown(text) {
  if (!text) return ''
  return marked.parse(text)
}

async function loadSessions() {
  sessionLoading.value = true
  try {
    const res = await getAdminAiSessions()
    sessions.value = (res && res.data) || []
  } catch (err) {
    console.error('加载对话列表失败:', err)
  } finally {
    sessionLoading.value = false
  }
}

async function selectSession(session) {
  selectedSessionId.value = session.sessionId
  historyLoading.value = true
  chatHistory.value = []
  try {
    const res = await getAdminAiChatHistory(session.sessionId, 200)
    chatHistory.value = (res && res.data) || []
    await nextTick()
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  } catch (err) {
    console.error('加载对话记录失败:', err)
  } finally {
    historyLoading.value = false
  }
}

function handleDelete(sessionId) {
  pendingDeleteSessionId = sessionId
  deleteDialogVisible.value = true
}

async function confirmDelete() {
  if (!pendingDeleteSessionId) return
  deleteLoading.value = true
  try {
    await deleteAdminAiSession(pendingDeleteSessionId)
    if (selectedSessionId.value === pendingDeleteSessionId) {
      selectedSessionId.value = null
      chatHistory.value = []
    }
    await loadSessions()
  } catch (err) {
    console.error('删除对话失败:', err)
  } finally {
    deleteLoading.value = false
    deleteDialogVisible.value = false
    pendingDeleteSessionId = null
  }
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.ai-history-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.session-list-card {
  height: 100%;
}

.session-list-card :deep(.el-card__body) {
  padding: 0;
  height: calc(100% - 56px);
  overflow-y: auto;
}

.session-list {
  min-height: 200px;
}

.session-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
  border-left: 3px solid #409EFF;
}

.session-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 30px;
}

.session-meta {
  font-size: 12px;
  color: #c0c4cc;
  display: flex;
  gap: 12px;
}

.session-delete {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .session-delete {
  opacity: 1;
}

.chat-card {
  height: 100%;
}

.chat-card :deep(.el-card__body) {
  padding: 0;
  height: calc(100% - 56px);
  overflow: hidden;
}

.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.no-session-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-more {
  text-align: center;
  padding: 12px;
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.chat-message.user .chat-avatar {
  background: #409EFF;
  color: #fff;
}

.chat-message.assistant .chat-avatar {
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  color: #fff;
}

.chat-bubble {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.chat-message.user .chat-bubble {
  background: #409EFF;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-message.assistant .chat-bubble {
  background: #f4f4f5;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.chat-bubble :deep(h2) { font-size: 16px; margin: 12px 0 8px; }
.chat-bubble :deep(h3) { font-size: 15px; margin: 10px 0 6px; }
.chat-bubble :deep(ul), .chat-bubble :deep(ol) { padding-left: 20px; margin: 6px 0; }
.chat-bubble :deep(li) { margin: 4px 0; }
.chat-bubble :deep(table) { border-collapse: collapse; width: 100%; margin: 8px 0; }
.chat-bubble :deep(th), .chat-bubble :deep(td) { border: 1px solid #dcdfe6; padding: 6px 10px; text-align: left; }
.chat-bubble :deep(th) { background: #f5f7fa; }
.chat-bubble :deep(code) { background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 4px; font-size: 13px; }
</style>