<template>
  <div class="ai-assistant-container">
    <div class="chat-header">
      <h3>AI 学习助手</h3>
      <el-button type="text" @click="clearChat">清空对话</el-button>
    </div>

    <div class="chat-messages" ref="chatMessages">
      <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
        <div class="message-avatar">
          <el-avatar :size="32" :icon="msg.role === 'user' ? 'UserFilled' : 'Cpu'" />
        </div>
        <div class="message-content">
          <div class="message-text" v-html="msg.content"></div>
          <div v-if="msg.status" class="message-status">{{ msg.status }}</div>
        </div>
      </div>
      <div v-if="loading" class="message-item assistant">
        <div class="message-avatar">
          <el-avatar :size="32" icon="Cpu" />
        </div>
        <div class="message-content">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="输入你的问题，例如：帮我推荐几本适合我的书..."
        @keydown.enter.exact.prevent="sendMessage"
      />
      <el-button type="primary" :disabled="!inputMessage.trim() || loading" @click="sendMessage">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onBeforeUnmount } from 'vue'
import { marked } from 'marked'
import { chatStream } from '@/api/ai'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const chatMessages = ref(null)
const sessionId = ref(generateSessionId())
let abortController = null

function generateSessionId() {
  return 'user_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMessages.value) {
      chatMessages.value.scrollTop = chatMessages.value.scrollHeight
    }
  })
}

function sendMessage() {
  const msg = inputMessage.value.trim()
  if (!msg || loading.value) return

  messages.value.push({ role: 'user', content: escapeHtml(msg) })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  const assistantMsg = { role: 'assistant', content: '', status: '' }
  messages.value.push(assistantMsg)

  abortController = new AbortController()

  chatStream(
    msg,
    null,
    sessionId.value,
    (status) => {
      assistantMsg.status = status
      scrollToBottom()
    },
    (chunk) => {
      assistantMsg.content += chunk
      assistantMsg.content = marked.parse(assistantMsg.content)
      scrollToBottom()
    },
    () => {
      assistantMsg.status = ''
      loading.value = false
      scrollToBottom()
    },
    (error) => {
      assistantMsg.content = '<span style="color: red;">抱歉，AI服务暂时不可用，请稍后再试。</span>'
      assistantMsg.status = ''
      loading.value = false
      scrollToBottom()
    },
    abortController.signal
  )
}

function clearChat() {
  messages.value = []
  sessionId.value = generateSessionId()
}

function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

onBeforeUnmount(() => {
  if (abortController) {
    abortController.abort()
  }
})
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 8px;
}

.message-content {
  max-width: 70%;
}

.message-item.user .message-content {
  text-align: right;
}

.message-text {
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.6;
  word-break: break-word;
}

.message-item.user .message-text {
  background: #409eff;
  color: #fff;
}

.message-item.assistant .message-text {
  background: #f4f4f5;
  color: #303133;
}

.message-status {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;
}

.chat-input .el-textarea {
  flex: 1;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 10px 14px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }
.typing-indicator span:nth-child(3) { animation-delay: 0s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>