import request from '@/utils/request'
import { fetchWithAuth } from '@/utils/fetchWithAuth'

export const getDashboardStats = () => request.get('/dashboard/admin/stats')

// ==================== 通用 SSE 流式读取器 ====================

function readSSEStream(response, { onStatus, onMessage, onDone, onError }) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  const read = () => {
    reader.read().then(({ done, value }) => {
      if (done) {
        onDone && onDone()
        return
      }
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let currentEvent = 'message'
      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data === '[DONE]') {
            onDone && onDone()
            return
          }
          if (currentEvent === 'status') {
            onStatus && onStatus(data)
          } else if (currentEvent === 'error') {
            onError && onError(data)
          } else {
            onMessage(data)
          }
        }
      }
      read()
    }).catch(err => {
      onError && onError(err)
    })
  }
  read()
}

// ==================== 管理端 AI 接口 ====================

export const getAdminAiSessions = () => {
  return fetchWithAuth('/api/ai/admin/chat/sessions').then(res => res.json())
}

export const getAdminAiChatHistory = (sessionId, limit = 100) => {
  return fetchWithAuth(`/api/ai/admin/chat/history/${sessionId}?limit=${limit}`).then(res => res.json())
}

export const deleteAdminAiSession = (sessionId) => {
  return fetchWithAuth(`/api/ai/admin/chat/sessions/${sessionId}`, { method: 'DELETE' }).then(res => res.json())
}

export const chatStream = ({ message, context, sessionId, onStatus, onMessage, onDone, onError, signal }) => {
  if (typeof onMessage !== 'function') {
    throw new Error('chatStream: onMessage 回调函数为必填参数')
  }

  return fetchWithAuth('/api/ai/admin/chat', {
    method: 'POST',
    body: JSON.stringify({ message, context, sessionId }),
    signal
  }).then(response => {
    readSSEStream(response, { onStatus, onMessage, onDone, onError })
  }).catch(err => {
    onError && onError(err)
  })
}

// ==================== 教师端 AI 接口 ====================

export const getTeacherAiSessions = () => {
  return fetchWithAuth('/api/ai/chat/sessions').then(res => res.json())
}

export const getTeacherAiChatHistory = (sessionId, limit = 100) => {
  return fetchWithAuth(`/api/ai/chat/history/${sessionId}?limit=${limit}`).then(res => res.json())
}

export const deleteTeacherAiSession = (sessionId) => {
  return fetchWithAuth(`/api/ai/chat/sessions/${sessionId}`, { method: 'DELETE' }).then(res => res.json())
}

export const teacherChatStream = ({ message, context, sessionId, onStatus, onMessage, onDone, onError, signal }) => {
  if (typeof onMessage !== 'function') {
    throw new Error('teacherChatStream: onMessage 回调函数为必填参数')
  }

  return fetchWithAuth('/api/ai/teacher/chat', {
    method: 'POST',
    body: JSON.stringify({ message, context, sessionId }),
    signal
  }).then(response => {
    readSSEStream(response, { onStatus, onMessage, onDone, onError })
  }).catch(err => {
    onError && onError(err)
  })
}