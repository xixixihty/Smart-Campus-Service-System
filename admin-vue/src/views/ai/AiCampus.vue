<template>
  <div class="ai-campus-page">
    <div class="page-header">
      <div class="header-left">
        <h2><el-icon><DataAnalysis /></el-icon> 校园数据概览</h2>
        <span class="header-subtitle">基于AI的校园运营数据智能分析平台</span>
      </div>
      <div class="header-actions">
        <span class="last-update" v-if="lastUpdateTime">上次更新：{{ lastUpdateTime }}</span>
        <el-button type="primary" :loading="loading" @click="fetchAllData">
          <el-icon><Refresh /></el-icon>刷新数据
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="(item, index) in statCards" :key="item.label">
        <div class="stat-card" :style="{ animationDelay: (index * 0.1) + 's' }">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" :style="{ background: item.bg }">
              <el-icon :size="28" :color="item.color"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                <span class="count-num">{{ item.displayValue }}</span>
              </div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
          <div class="stat-trend" v-if="item.trend">
            <el-icon :color="item.trend > 0 ? '#52c41a' : '#ff4d4f'">
              <component :is="item.trend > 0 ? 'Top' : 'Bottom'" />
            </el-icon>
            <span :style="{ color: item.trend > 0 ? '#52c41a' : '#ff4d4f' }">{{ Math.abs(item.trend) }}%</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="never" class="report-card" v-loading="reportLoading">
          <template #header>
            <div class="card-header">
              <span class="card-title"><el-icon><Cpu /></el-icon> AI 智能分析报告</span>
              <el-tag type="success" size="small">AI 生成</el-tag>
            </div>
          </template>
          <div class="ai-report" v-if="reportContent">
            <div class="report-content" v-html="reportContent"></div>
          </div>
          <div class="report-empty" v-else>
            <el-icon :size="48" color="#c0c4cc"><Document /></el-icon>
            <p>点击右上角"刷新数据"获取AI智能分析报告</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="quick-stats-card">
          <template #header>
            <div class="card-header">
              <span class="card-title"><el-icon><TrendCharts /></el-icon> 数据分布</span>
            </div>
          </template>
          <div class="quick-stats">
            <div class="quick-stat-item" v-for="item in quickStats" :key="item.label">
              <div class="qs-label">{{ item.label }}</div>
              <el-progress
                :percentage="item.percent"
                :color="item.color"
                :stroke-width="8"
                :show-text="false"
              />
              <div class="qs-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
            <p>我可以帮你分析校园数据、回答运营问题</p>
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
import { getCampusOverview, getDashboardStats, chatStream } from '@/api/ai'

const loading = ref(false)
const reportLoading = ref(false)
const reportContent = ref('')
const lastUpdateTime = ref('')

const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const streamingContent = ref('')
const chatMessagesRef = ref(null)

const quickQuestions = [
  '当前校园学生总数是多少？',
  '分析一下图书借阅情况',
  '各学院学生分布如何？',
  '给出校园运营优化建议'
]

const statCards = reactive([
  { label: '在校学生', value: 0, displayValue: '--', icon: 'User', color: '#1890ff', bg: '#e6f7ff', trend: 0 },
  { label: '在职教师', value: 0, displayValue: '--', icon: 'Avatar', color: '#52c41a', bg: '#f6ffed', trend: 0 },
  { label: '图书总量', value: 0, displayValue: '--', icon: 'CollectionTag', color: '#fa8c16', bg: '#fff7e6', trend: 0 },
  { label: '座位总数', value: 0, displayValue: '--', icon: 'Place', color: '#eb2f96', bg: '#fff0f6', trend: 0 }
])

const quickStats = reactive([
  { label: '学生/教师比', value: '--', percent: 0, color: '#1890ff' },
  { label: '图书/学生比', value: '--', percent: 0, color: '#52c41a' },
  { label: '座位利用率', value: '--', percent: 0, color: '#fa8c16' }
])

const formatNumber = (num) => {
  if (num === null || num === undefined) return '--'
  return Number(num).toLocaleString()
}

const fetchStats = async () => {
  try {
    const res = await getDashboardStats()
    const data = res.data || {}
    statCards[0].value = data.studentCount || 0
    statCards[0].displayValue = formatNumber(data.studentCount)
    statCards[1].value = data.teacherCount || 0
    statCards[1].displayValue = formatNumber(data.teacherCount)
    statCards[2].value = data.bookCount || 0
    statCards[2].displayValue = formatNumber(data.bookCount)
    statCards[3].value = data.seatCount || 0
    statCards[3].displayValue = formatNumber(data.seatCount)

    const studentCount = data.studentCount || 1
    const teacherCount = data.teacherCount || 0
    const bookCount = data.bookCount || 0
    const seatCount = data.seatCount || 0

    quickStats[0].value = `1:${(studentCount / Math.max(teacherCount, 1)).toFixed(1)}`
    quickStats[0].percent = Math.min(Math.round((teacherCount / Math.max(studentCount, 1)) * 100), 100)

    quickStats[1].value = `${(bookCount / Math.max(studentCount, 1)).toFixed(1)} 册/人`
    quickStats[1].percent = Math.min(Math.round((bookCount / Math.max(studentCount, 1)) * 20), 100)

    quickStats[2].value = `${seatCount} 个`
    quickStats[2].percent = Math.min(Math.round((seatCount / Math.max(studentCount, 1)) * 100), 100)
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

const fetchReport = async () => {
  reportLoading.value = true
  try {
    const res = await getCampusOverview()
    reportContent.value = res.data?.report || res.data || ''
  } catch (e) {
    console.error('获取AI报告失败:', e)
  } finally {
    reportLoading.value = false
  }
}

const fetchAllData = async () => {
  loading.value = true
  try {
    await Promise.all([fetchStats(), fetchReport()])
    const now = new Date()
    lastUpdateTime.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  } finally {
    loading.value = false
  }
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
    'campus',
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

onMounted(fetchAllData)
</script>

<style scoped>
.ai-campus-page {
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.last-update {
  font-size: 12px;
  opacity: 0.8;
}

.header-actions .el-button {
  --el-button-bg-color: rgba(255,255,255,0.2);
  --el-button-border-color: rgba(255,255,255,0.3);
  --el-button-hover-bg-color: rgba(255,255,255,0.3);
  --el-button-text-color: #fff;
}

.stat-row {
  margin-bottom: 0;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  animation: slideUp 0.5s ease both;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #1890ff, #52c41a, #fa8c16, #eb2f96);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  margin-bottom: 4px;
}

.count-num {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  font-family: 'DIN', 'Monaco', monospace;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
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
  min-height: 300px;
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
  color: #1890ff;
}

.report-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #c0c4cc;
}

.report-empty p {
  margin-top: 12px;
  font-size: 14px;
}

.quick-stats-card {
  border-radius: 12px;
  overflow: hidden;
}

.quick-stats-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.quick-stats {
  padding: 8px 0;
}

.quick-stat-item {
  padding: 12px 0;
}

.quick-stat-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 8px;
  padding-bottom: 16px;
}

.qs-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.qs-value {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
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
  background: linear-gradient(135deg, #667eea, #764ba2);
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