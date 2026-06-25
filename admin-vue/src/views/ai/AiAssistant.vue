<template>
  <AiChatPanel
    title="智慧助手"
    subtitle="数据分析 · 教学管理 · 图书统计 · 校园监控"
    welcome-text="我可以帮你查询校园统计数据、分析教学数据、管理图书借阅等"
    :quick-questions="[
      '查询校园统计', '教师数据分析', '学生数据分析',
      '图书借阅统计', '座位使用情况', '课程安排分析',
      '请假审批统计', '成绩分布分析'
    ]"
    session-prefix="admin"
    :chat-stream="chatStream"
    :get-sessions="getAdminAiSessions"
    :get-chat-history="getAdminAiChatHistory"
    :delete-session="deleteAdminAiSession"
  />
</template>

<script setup>
import AiChatPanel from '@/components/AiChatPanel.vue'
import {
  chatStream,
  getAdminAiSessions,
  getAdminAiChatHistory,
  deleteAdminAiSession
} from '@/api/ai'
</script>