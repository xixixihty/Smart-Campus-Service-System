<template>
  <div class="ai-page">
    <div class="page-header">
      <div class="header-left">
        <h2><el-icon><PieChart /></el-icon> 成绩数据分析</h2>
        <span class="header-subtitle">基于AI的学生成绩智能分析</span>
      </div>
    </div>

    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable size="large">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="queryForm.collegeId" placeholder="请选择学院" clearable size="large">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchData" size="large">
            <el-icon><Cpu /></el-icon>AI 分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="report-card" style="margin-top: 20px" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title"><el-icon><Cpu /></el-icon> AI 成绩分析报告</span>
          <el-tag type="success" size="small">AI 生成</el-tag>
        </div>
      </template>
      <div class="ai-report" v-if="reportContent">
        <div class="report-content" v-html="reportContent"></div>
      </div>
      <div class="report-empty" v-else>
        <el-icon :size="48" color="#c0c4cc"><Document /></el-icon>
        <p>请选择学期和学院后点击"AI 分析"获取成绩分析报告</p>
      </div>
    </el-card>

    <el-card shadow="never" class="chat-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title"><el-icon><ChatDotRound /></el-icon> AI 智能对话</span>
          <el-tag type="warning" size="small">实时对话</el-tag>
        </div>
      </template>
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-if="chatMessages.length === 0 && !chatLoading" class="chat-welcome">
            <div class="welcome-icon">
              <el-icon :size="48"><Cpu /></el-icon>
            </div>
            <h3>你好，我是校园AI助手</h3>
            <p>我可以帮你分析学生成绩、发现学习趋势</p>
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
              <el-icon :size="20" v-if="msg.role === 'user'"><UserFilled /></el-icon>
              <el-icon :size="20" v-else><Cpu /></el-icon>
            </div>
            <div class="chat-bubble" v-html="msg.content"></div>
          </div>
          <div v-if="chatLoading" class="chat-message assistant">
            <div class="chat-avatar">
              <el-icon :size="20"><Cpu /></el-icon>
            </div>
            <div class="chat-bubble streaming">
              <span v-if="streamingContent" v-html="streamingContent"></span>
              <span v-else class="thinking-dots">
                <span class="dot">●</span><span class="dot">●</span><span class="dot">●</span>
              </span>
              <span class="cursor-blink">|</span>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <el-input
            v-model="chatInput"
            placeholder="输入问题，与AI助手对话..."
            :disabled="chatLoading"
            @keyup.enter="sendMessage"
            clearable
            size="large"
          >
            <template #append>
              <el-button
                :loading="chatLoading"
                :disabled="!chatInput.trim()"
                @click="sendMessage"
                type="primary"
              >
                <el-icon><Promotion /></el-icon>发送
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getScoreAnalysis, chatStream } from '@/api/ai'
import { getSemesterList } from '@/api/semester'
import { getCollegeList } from '@/api/college'

const loading = ref(false)
const reportContent = ref('')
const semesterOptions = ref([])
const collegeOptions = ref([])

const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const streamingContent = ref('')
const chatMessagesRef = ref(null)

const queryForm = reactive({ semesterId: '', collegeId: '' })

const quickQuestions = [
  '当前学期成绩整体分布如何？',
  '哪些课程及格率较低？',
  '各学院成绩对比分析',
  '给出成绩提升建议'
]

const loadOptions = async () => {
  const [semesterRes, collegeRes] = await Promise.all([
    getSemesterList({ pageNum: 1, pageSize: 100 }),
    getCollegeList({ pageNum: 1, pageSize: 100 })
  ])
  semesterOptions.value = semesterRes.data.list || []
  collegeOptions.value = collegeRes.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getScoreAnalysis(queryForm)
    reportContent.value = res.data?.report || res.data || ''
  } finally { loading.value = false }
}

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

  chatMessages.value.push({ role: 'user', content: msg.replace(/\n/g, '<br>') })
  chatInput.value = ''
  chatLoading.value = true
  streamingContent.value = ''
  await scrollToBottom()

  chatStream(
    msg,
    'score',
    (data) => {
      streamingContent.value += data
      scrollToBottom()
    },
    () => {
      chatMessages.value.push({ role: 'assistant', content: streamingContent.value })
      streamingContent.value = ''
      chatLoading.value = false
      scrollToBottom()
    },
    () => {
      chatMessages.value.push({ role: 'assistant', content: '抱歉，对话服务暂时不可用，请稍后重试。' })
      streamingContent.value = ''
      chatLoading.value = false
    }
  )
}

onMounted(loadOptions)
</script>

<style scoped>
.ai-page {
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
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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
}

.query-card {
  border-radius: 12px;
  overflow: hidden;
}

.query-card :deep(.el-card__body) {
  padding: 20px 24px 4px;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.query-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.report-card {
  border-radius: 12px;
  overflow: hidden;
}

.report-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.ai-report {
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}

.report-content {
  line-height: 2;
  font-size: 14px;
  color: #333;
  white-space: pre-wrap;
}

.report-content :deep(h1),
.report-content :deep(h2),
.report-content :deep(h3) {
  color: #1a1a2e;
  margin: 16px 0 8px;
}

.report-content :deep(strong) {
  color: #4facfe;
}

.report-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #c0c4cc;
}

.report-empty p {
  margin-top: 12px;
  font-size: 14px;
}

.chat-card {
  border-radius: 12px;
  overflow: hidden;
}

.chat-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.chat-container {
  display: flex;
  flex-direction: column;
}

.chat-messages {
  height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: linear-gradient(180deg, #f8f9ff 0%, #f0f2f5 100%);
  border-radius: 8px;
  margin-bottom: 16px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 2px;
}

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 16px;
}

.chat-welcome h3 {
  margin: 0 0 8px;
  color: #303133;
  font-size: 18px;
}

.chat-welcome p {
  color: #909399;
  font-size: 14px;
  margin: 0 0 20px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  animation: msgIn 0.3s ease;
}

@keyframes msgIn {
  from { opacity: 0; transform: translateY(8px); }
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
}

.chat-message.user .chat-bubble {
  background: linear-gradient(135deg, #1890ff, #096dd9);
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
  color: #1890ff;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.chat-input-area {
  padding: 0 4px;
}
</style>