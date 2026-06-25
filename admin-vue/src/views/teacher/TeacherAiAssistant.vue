<template>
  <AiChatPanel
    title="智慧助手"
    subtitle="教学辅助 · 成绩分析 · 课表查询 · 审批管理"
    welcome-text="我可以帮你查询教学数据、分析成绩、管理课表等"
    :quick-questions="[
      '查询我的课表', '成绩录入统计', '待审批请假列表',
      '调课申请状态', '学生成绩分布', '通知公告查询'
    ]"
    session-prefix="teacher"
    :chat-stream="teacherChatStream"
    :get-sessions="getTeacherAiSessions"
    :get-chat-history="getTeacherAiChatHistory"
    :delete-session="deleteTeacherAiSession"
  />
</template>

<script setup>
import AiChatPanel from '@/components/AiChatPanel.vue'
import {
  teacherChatStream,
  getTeacherAiSessions,
  getTeacherAiChatHistory,
  deleteTeacherAiSession
} from '@/api/ai'
</script>