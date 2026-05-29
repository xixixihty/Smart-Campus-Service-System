<template>
  <div class="ai-assistant-page">
    <div class="page-header">
      <div class="header-left">
        <h2><el-icon><Cpu /></el-icon> AI 智慧助手</h2>
        <p class="header-subtitle">数据分析 · 教学管理 · 图书统计 · 校园监控</p>
      </div>
      <div class="header-actions">
        <el-button @click="toggleSidebar" :icon="sidebarCollapsed ? Expand : Fold" text>
          {{ sidebarCollapsed ? '展开历史' : '折叠历史' }}
        </el-button>
        <el-button @click="newChat" :icon="Plus" :disabled="loading">新建对话</el-button>
        <el-button @click="clearChat" :icon="Delete">清空对话</el-button>
      </div>
    </div>

    <el-card shadow="never" class="main-card">
      <div class="main-layout">
        <transition name="slide">
          <div v-show="!sidebarCollapsed" class="history-sidebar">
            <div class="sidebar-header">
              <span class="sidebar-title">
                <el-icon><ChatLineRound /></el-icon> 对话历史
              </span>
              <el-button text size="small" @click="loadSessions" :loading="sessionLoading" :icon="Refresh" />
            </div>
            <div v-loading="sessionLoading" class="session-list">
              <div
                v-for="session in sessions"
                :key="session.sessionId"
                :class="['session-item', { active: currentSessionId === session.sessionId }]"
                @click="selectSession(session)"
              >
                <div class="session-title">{{ session.title || '新对话' }}</div>
                <div class="session-meta">
                  <span>{{ session.messageCount }} 条</span>
                  <span>{{ formatTime(session.lastMessageAt) }}</span>
                </div>
                <el-button
                  class="session-delete"
                  :icon="Delete"
                  circle
                  size="small"
                  @click.stop="handleSessionDelete(session.sessionId)"
                />
              </div>
              <el-empty v-if="!sessionLoading && sessions.length === 0" description="暂无对话记录" :image-size="60" />
            </div>
          </div>
        </transition>

        <div class="chat-area">
          <div class="chat-messages" ref="chatMessagesRef">
            <div v-if="messages.length === 0 && !loading" class="chat-welcome">
              <div class="welcome-icon">
                <el-icon :size="48"><Cpu /></el-icon>
              </div>
              <h3>你好，我是AI智慧助手</h3>
              <p>我可以帮你查询校园统计数据、分析教学数据、管理图书借阅等</p>
              <div class="quick-questions">
                <el-tag
                  v-for="q in quickQuestions"
                  :key="q"
                  class="quick-tag"
                  @click="sendQuickQuestion(q)"
                >{{ q }}</el-tag>
              </div>
            </div>

            <div v-for="(msg, index) in messages" :key="index" :class="['chat-message', msg.role]">
              <div class="chat-avatar">
                <el-icon v-if="msg.role === 'user'"><User /></el-icon>
                <el-icon v-else><Cpu /></el-icon>
              </div>
              <div class="chat-bubble">
                <div v-html="msg.content"></div>
                <div v-if="msg.status" class="status-text">{{ msg.status }}</div>
              </div>
            </div>

            <div v-if="loading && streamingContent" class="chat-message assistant">
              <div class="chat-avatar">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="chat-bubble streaming">
                <span v-html="streamingContent"></span>
                <span class="cursor-blink">|</span>
              </div>
            </div>

            <div v-if="loading && !streamingContent" class="chat-message assistant">
              <div class="chat-avatar">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="chat-bubble">
                <div class="thinking-dots">
                  <span class="dot">●</span>
                  <span class="dot">●</span>
                  <span class="dot">●</span>
                </div>
              </div>
            </div>
          </div>

          <div class="chat-input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入你的问题，按 Enter 发送..."
              @keydown.enter.exact.prevent="sendMessage"
              :disabled="loading || isViewingHistory"
            />
            <div class="input-actions">
              <span v-if="isViewingHistory" class="tip-text warn">当前正在查看历史记录，如需继续对话请点击"新建对话"</span>
              <span v-else class="tip-text">Enter 发送，Shift+Enter 换行</span>
              <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputMessage.trim() || isViewingHistory">
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

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
import { ref, nextTick, onBeforeUnmount, onMounted } from 'vue'
import { Cpu, User, Delete, Promotion, Refresh, Plus, Expand, Fold, ChatLineRound } from '@element-plus/icons-vue'
import { marked } from 'marked'
import { chatStream, getAdminAiSessions, getAdminAiChatHistory, deleteAdminAiSession } from '@/api/ai'

marked.setOptions({ breaks: true, gfm: true })

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const streamingContent = ref('')
const chatMessagesRef = ref(null)
const sessionId = ref(generateSessionId())
let abortController = null

const sidebarCollapsed = ref(false)
const sessions = ref([])
const sessionLoading = ref(false)
const currentSessionId = ref(null)
const isViewingHistory = ref(false)
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
let pendingDeleteSessionId = null

const quickQuestions = [
  '查询校园统计',
  '教师数据分析',
  '学生数据分析',
  '图书借阅统计',
  '座位使用情况',
  '课程安排分析',
  '请假审批统计',
  '成绩分布分析'
]

function generateSessionId() {
  return 'admin_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
}

function renderMarkdown(text) {
  if (!text) return ''
  return marked.parse(text)
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return (d.getMonth() + 1) + '月' + d.getDate() + '日'
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function newChat() {
  if (abortController) abortController.abort()
  messages.value = []
  streamingContent.value = ''
  loading.value = false
  sessionId.value = generateSessionId()
  currentSessionId.value = null
  isViewingHistory.value = false
}

function clearChat() {
  if (abortController) abortController.abort()
  messages.value = []
  streamingContent.value = ''
  loading.value = false
  currentSessionId.value = null
  isViewingHistory.value = false
}

function sendQuickQuestion(question) {
  inputMessage.value = question
  sendMessage()
}

function sendMessage() {
  const msg = inputMessage.value.trim()
  if (!msg || loading.value || isViewingHistory.value) return

  messages.value.push({ role: 'user', content: renderMarkdown(msg) })
  inputMessage.value = ''
  loading.value = true
  streamingContent.value = ''
  scrollToBottom()

  const statusMsg = { role: 'assistant', content: '', status: '' }
  messages.value.push(statusMsg)

  abortController = new AbortController()
  let fullContent = ''

  chatStream({
    message: msg,
    context: null,
    sessionId: sessionId.value,
    onStatus: (status) => {
      statusMsg.status = status
      scrollToBottom()
    },
    onMessage: (chunk) => {
      fullContent += chunk
      streamingContent.value = renderMarkdown(fullContent)
      scrollToBottom()
    },
    onDone: () => {
      statusMsg.content = renderMarkdown(fullContent)
      statusMsg.status = ''
      streamingContent.value = ''
      loading.value = false
      loadSessions()
      scrollToBottom()
    },
    onError: () => {
      statusMsg.content = renderMarkdown('抱歉，AI服务暂时不可用，请稍后再试。')
      statusMsg.status = ''
      streamingContent.value = ''
      loading.value = false
      scrollToBottom()
    },
    signal: abortController.signal
  })
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
  currentSessionId.value = session.sessionId
  isViewingHistory.value = true
  messages.value = []
  loading.value = true
  try {
    const res = await getAdminAiChatHistory(session.sessionId, 200)
    const history = (res && res.data) || []
    messages.value = history.map(msg => ({
      role: msg.role,
      content: renderMarkdown(msg.content)
    }))
    await nextTick()
    scrollToBottom()
  } catch (err) {
    console.error('加载对话记录失败:', err)
  } finally {
    loading.value = false
  }
}

function handleSessionDelete(sessionId) {
  pendingDeleteSessionId = sessionId
  deleteDialogVisible.value = true
}

async function confirmDelete() {
  if (!pendingDeleteSessionId) return
  deleteLoading.value = true
  try {
    await deleteAdminAiSession(pendingDeleteSessionId)
    if (currentSessionId.value === pendingDeleteSessionId) {
      currentSessionId.value = null
      messages.value = []
      isViewingHistory.value = false
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

onBeforeUnmount(() => {
  if (abortController) abortController.abort()
})
</script>

<style scoped>
.ai-assistant-page {
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
  color: #303133;
}

.header-subtitle {
  margin: 0;
  font-size: 14px;
  color: #409EFF;
  font-weight: 600;
  letter-spacing: 1px;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.main-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.main-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.history-sidebar {
  width: 260px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.sidebar-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.session-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.session-item:hover {
  background-color: #f0f2f5;
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
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .session-delete {
  opacity: 1;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-welcome {
  text-align: center;
  padding: 60px 20px;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.chat-welcome h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.chat-welcome p {
  margin: 0 0 24px 0;
  color: #909399;
  font-size: 14px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.quick-tag {
  cursor: pointer;
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 20px;
  transition: all 0.2s;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.chat-input-area {
  padding: 12px 20px 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.tip-text {
  font-size: 12px;
  color: #c0c4cc;
}

.tip-text.warn {
  color: #e6a23c;
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

.chat-bubble .status-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chat-bubble.streaming {
  background: #f0f5ff;
}

.chat-bubble :deep(h2) { font-size: 16px; margin: 12px 0 8px; }
.chat-bubble :deep(h3) { font-size: 15px; margin: 10px 0 6px; }
.chat-bubble :deep(ul), .chat-bubble :deep(ol) { padding-left: 20px; margin: 6px 0; }
.chat-bubble :deep(li) { margin: 4px 0; }
.chat-bubble :deep(table) { border-collapse: collapse; width: 100%; margin: 8px 0; }
.chat-bubble :deep(th), .chat-bubble :deep(td) { border: 1px solid #dcdfe6; padding: 6px 10px; text-align: left; }
.chat-bubble :deep(th) { background: #f5f7fa; }
.chat-bubble :deep(code) { background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.chat-bubble :deep(pre) { background: #282c34; color: #abb2bf; padding: 12px; border-radius: 8px; overflow-x: auto; }
.chat-bubble :deep(pre code) { background: none; padding: 0; color: inherit; }
.chat-bubble :deep(strong) { font-weight: 600; }
.chat-bubble :deep(blockquote) { border-left: 3px solid #409EFF; padding-left: 12px; margin: 8px 0; color: #606266; }

.cursor-blink {
  animation: blink 1s infinite;
  color: #409EFF;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.thinking-dots {
  display: flex;
  gap: 6px;
  padding: 4px 0;
}

.dot {
  animation: dotPulse 1.4s infinite;
  font-size: 12px;
  color: #909399;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  width: 0;
  opacity: 0;
}
</style>