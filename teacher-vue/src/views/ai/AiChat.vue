<template>
  <div class="ai-chat-page">
    <div class="page-header">
      <div class="header-left">
        <h2><el-icon><ChatDotRound /></el-icon> AI 教学助手</h2>
        <p class="header-subtitle">智能教学管理，助力高效工作</p>
      </div>
      <div class="header-actions">
        <el-button @click="clearChat" :icon="Delete">清空对话</el-button>
      </div>
    </div>

    <el-card shadow="never" class="chat-card">
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-if="chatMessages.length === 0 && !chatLoading" class="chat-welcome">
            <div class="welcome-icon">
              <el-icon :size="48"><Cpu /></el-icon>
            </div>
            <h3>你好，我是你的 AI 教学助手</h3>
            <p>我可以帮你查询课表、分析学生成绩、查看待批请假、管理调课记录等</p>
            <div class="quick-questions">
              <el-tag
                v-for="q in quickQuestions"
                :key="q"
                class="quick-tag"
                @click="sendQuickQuestion(q)"
              >{{ q }}</el-tag>
            </div>
          </div>

          <div v-for="(msg, index) in chatMessages" :key="index" :class="['chat-message', msg.role]">
            <div class="chat-avatar">
              <el-icon v-if="msg.role === 'user'"><User /></el-icon>
              <el-icon v-else><Cpu /></el-icon>
            </div>
            <div class="chat-bubble" v-html="msg.content"></div>
          </div>

          <div v-if="chatLoading && streamingContent" class="chat-message assistant">
            <div class="chat-avatar">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="chat-bubble streaming">
              <span v-html="streamingContent"></span>
              <span class="cursor-blink">|</span>
            </div>
          </div>

          <div v-if="chatLoading && !streamingContent" class="chat-message assistant">
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
            v-model="chatInput"
            type="textarea"
            :rows="3"
            placeholder="输入你的问题，按 Enter 发送..."
            @keydown.enter.exact.prevent="sendMessage"
            :disabled="chatLoading"
          />
          <div class="input-actions">
            <span class="tip-text">Enter 发送，Shift+Enter 换行</span>
            <el-button type="primary" @click="sendMessage" :loading="chatLoading" :disabled="!chatInput.trim()">
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ChatDotRound, Cpu, User, Promotion, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { chatStream } from '@/api/ai'

marked.setOptions({ breaks: true, gfm: true })

const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const streamingContent = ref('')
const chatMessagesRef = ref(null)
let rawContent = ''

const escapeHtml = (text) => text.replace(/[&<>"']/g, (c) => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[c]))

const renderMarkdown = (text) => (text ? marked.parse(text) : '')
const renderUserText = (text) => escapeHtml(text).replace(/\n/g, '<br>')

const quickQuestions = [
  '我的课表是什么？',
  '我有多少学生？',
  '目前有哪些待审批的请假？',
  '我的调课记录有哪些？'
]

const scrollToBottom = async () => {
  await nextTick()
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

const sendQuickQuestion = (q) => {
  chatInput.value = q
  sendMessage()
}

const sendMessage = async () => {
  const msg = chatInput.value.trim()
  if (!msg || chatLoading.value) return

  chatMessages.value.push({ role: 'user', content: renderUserText(msg) })
  chatInput.value = ''
  chatLoading.value = true
  streamingContent.value = ''
  rawContent = ''
  await scrollToBottom()

  chatStream({
    message: msg,
    onMessage: (data) => {
      rawContent += data
      streamingContent.value = renderMarkdown(rawContent)
      scrollToBottom()
    },
    onDone: () => {
      chatMessages.value.push({ role: 'assistant', content: renderMarkdown(rawContent) })
      streamingContent.value = ''
      rawContent = ''
      chatLoading.value = false
      scrollToBottom()
    },
    onError: () => {
      chatMessages.value.push({ role: 'assistant', content: '抱歉，对话服务暂时不可用，请稍后重试。' })
      streamingContent.value = ''
      rawContent = ''
      chatLoading.value = false
    }
  })
}

const clearChat = () => {
  chatMessages.value = []
  streamingContent.value = ''
  rawContent = ''
  ElMessage.success('对话已清空')
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.ai-chat-page {
  padding: 0;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #409EFF, #337ecc);
  border-radius: 12px;
  color: #fff;
}

.header-left h2 {
  font-size: 22px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px 0;
  color: #fff;
}

.header-subtitle {
  font-size: 13px;
  opacity: 0.85;
  margin: 0;
}

.chat-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.chat-card :deep(.el-card__body) {
  padding: 0;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 280px);
  min-height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}

.chat-welcome {
  text-align: center;
  padding: 60px 20px;
}

.welcome-icon {
  margin-bottom: 16px;
  color: #409EFF;
}

.chat-welcome h3 {
  font-size: 20px;
  margin: 0 0 8px 0;
  color: #303133;
}

.chat-welcome p {
  font-size: 14px;
  color: #909399;
  margin: 0 0 24px 0;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.quick-tag {
  cursor: pointer;
  padding: 8px 16px;
  font-size: 13px;
  transition: all 0.3s;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.chat-message {
  display: flex;
  margin-bottom: 16px;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
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
  background: linear-gradient(135deg, #e6f7ff, #bae7ff);
  color: #1890ff;
}

.chat-message.assistant .chat-avatar {
  background: linear-gradient(135deg, #f0f5ff, #d6e4ff);
  color: #597ef7;
}

.chat-bubble {
  max-width: 72%;
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.7;
  word-break: break-word;
  font-size: 14px;
  margin: 0 12px;
}

.chat-message.user .chat-bubble {
  background: linear-gradient(135deg, #409EFF, #337ecc);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-message.assistant .chat-bubble {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.chat-bubble.streaming {
  min-width: 100px;
}

/* Markdown 内容样式 */
.chat-bubble :deep(p) { margin: 6px 0; }
.chat-bubble :deep(h2) { font-size: 16px; margin: 12px 0 8px; }
.chat-bubble :deep(h3) { font-size: 15px; margin: 10px 0 6px; }
.chat-bubble :deep(ul), .chat-bubble :deep(ol) { padding-left: 20px; margin: 6px 0; }
.chat-bubble :deep(li) { margin: 4px 0; }
.chat-bubble :deep(table) { border-collapse: collapse; width: 100%; margin: 8px 0; }
.chat-bubble :deep(th), .chat-bubble :deep(td) { border: 1px solid #dcdfe6; padding: 6px 10px; text-align: left; }
.chat-bubble :deep(th) { background: #f5f7fa; }
.chat-bubble :deep(code) { background: rgba(0, 0, 0, 0.06); padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.chat-bubble :deep(pre) { background: #282c34; color: #abb2bf; padding: 12px; border-radius: 8px; overflow-x: auto; }
.chat-bubble :deep(pre code) { background: none; padding: 0; color: inherit; }
.chat-bubble :deep(strong) { font-weight: 600; }
.chat-bubble :deep(blockquote) { border-left: 3px solid #409EFF; padding-left: 12px; margin: 8px 0; color: #606266; }

.thinking-dots {
  display: inline-flex;
  gap: 4px;
}

.thinking-dots .dot {
  animation: dotPulse 1.4s infinite both;
  font-size: 8px;
  color: #909399;
}

.thinking-dots .dot:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 80%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
}

.cursor-blink {
  animation: blink 1s infinite;
  color: #409EFF;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.chat-input-area {
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.tip-text {
  font-size: 12px;
  color: #909399;
}

.chat-input-area :deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: none;
}
</style>