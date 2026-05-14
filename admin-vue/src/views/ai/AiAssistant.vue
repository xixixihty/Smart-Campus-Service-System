<template>
  <div class="ai-assistant-page">
    <div class="page-header">
      <div class="header-left">
        <h2><el-icon><Cpu /></el-icon> AI 智慧助手</h2>
        <p class="header-subtitle">校园数据智能分析 · 教学质量评估 · 资源优化建议</p>
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
            <h3>你好，我是校园AI智慧助手</h3>
            <p>我可以帮你分析校园数据、评估教学质量、提供资源优化建议</p>
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
import { ref, nextTick, onBeforeUnmount } from 'vue'
import { Cpu, User, Delete, Promotion } from '@element-plus/icons-vue'
import { chatStream } from '@/api/ai'
import { marked } from 'marked'

marked.setOptions({
  breaks: true,
  gfm: true
})

const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const streamingContent = ref('')
const chatMessagesRef = ref(null)
let abortController = null

const quickQuestions = [
  '校园数据概览',
  '学生分布统计',
  '教学质量评估',
  '成绩数据分析',
  '资源利用分析',
  '借阅趋势查询',
  '教师职称分布',
  '请假统计查询'
]

function renderMarkdown(text) {
  if (!text) return ''
  return marked.parse(text)
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

function sendQuickQuestion(question) {
  chatInput.value = question
  sendMessage()
}

async function sendMessage() {
  const message = chatInput.value.trim()
  if (!message || chatLoading.value) return

  chatMessages.value.push({ role: 'user', content: renderMarkdown(message) })
  chatInput.value = ''
  chatLoading.value = true
  streamingContent.value = ''
  scrollToBottom()

  try {
    abortController = new AbortController()
    let fullContent = ''

    await chatStream(
      message,
      '',
      (chunk) => {
        fullContent += chunk
        streamingContent.value = renderMarkdown(fullContent)
        scrollToBottom()
      },
      () => {
        chatMessages.value.push({ role: 'assistant', content: renderMarkdown(fullContent) })
        streamingContent.value = ''
        chatLoading.value = false
        scrollToBottom()
      },
      (error) => {
        console.error('AI对话错误:', error)
        chatMessages.value.push({ role: 'assistant', content: '抱歉，服务暂时不可用，请稍后重试。' })
        streamingContent.value = ''
        chatLoading.value = false
      },
      abortController.signal
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    chatMessages.value.push({ role: 'assistant', content: '抱歉，发送失败，请重试。' })
    chatLoading.value = false
  }
}

function clearChat() {
  if (abortController) {
    abortController.abort()
  }
  chatMessages.value = []
  streamingContent.value = ''
  chatLoading.value = false
}

onBeforeUnmount(() => {
  if (abortController) {
    abortController.abort()
  }
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
}

.header-subtitle {
  margin: 0;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.chat-input-area {
  border-top: 1px solid #ebeef5;
  padding: 16px 20px;
  flex-shrink: 0;
}

.chat-input-area :deep(.el-textarea__inner) {
  resize: none;
  border-radius: 8px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.tip-text {
  font-size: 12px;
  color: #c0c4cc;
}
</style>