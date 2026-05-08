<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><PieChart /></el-icon> 成绩数据分析</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable>
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="queryForm.collegeId" placeholder="请选择学院" clearable>
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchData">
            <el-icon><Cpu /></el-icon>AI 分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 20px" v-loading="loading">
      <template #header>
        <span><el-icon><Cpu /></el-icon> AI 成绩分析报告</span>
      </template>
      <div class="ai-report" v-html="reportContent"></div>
      <el-empty v-if="!reportContent" description="请选择条件后点击 AI 分析" />
    </el-card>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span><el-icon><ChatDotRound /></el-icon> AI 智能对话</span>
      </template>
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-for="(msg, index) in chatMessages" :key="index" :class="['chat-message', msg.role]">
            <div class="chat-avatar">
              <el-icon :size="24" v-if="msg.role === 'user'"><UserFilled /></el-icon>
              <el-icon :size="24" v-else><Cpu /></el-icon>
            </div>
            <div class="chat-bubble" v-html="msg.content"></div>
          </div>
          <div v-if="chatLoading" class="chat-message assistant">
            <div class="chat-avatar">
              <el-icon :size="24"><Cpu /></el-icon>
            </div>
            <div class="chat-bubble streaming">
              <span v-html="streamingContent || '思考中...'"></span>
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
          >
            <template #append>
              <el-button :loading="chatLoading" :disabled="!chatInput.trim()" @click="sendMessage" type="primary">
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
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.ai-report { line-height: 1.8; white-space: pre-wrap; }

.chat-container {
  display: flex;
  flex-direction: column;
}

.chat-messages {
  height: 360px;
  overflow-y: auto;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
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
  background: #e6f7ff;
  color: #1890ff;
}

.chat-message.user .chat-avatar {
  background: #e6f7ff;
  color: #1890ff;
}

.chat-message.assistant .chat-avatar {
  background: #f0f5ff;
  color: #597ef7;
}

.chat-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.6;
  word-break: break-word;
}

.chat-message.user .chat-bubble {
  background: #1890ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-message.assistant .chat-bubble {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 4px;
}

.chat-bubble.streaming {
  min-width: 80px;
}

.cursor-blink {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.chat-input-area {
  padding: 0 4px;
}
</style>
