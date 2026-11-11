import request from '@/utils/request'
import { fetchWithAuth } from '@/utils/fetchWithAuth'

export const getDashboardStats = () => request.get('/dashboard/admin/stats')

// ==================== 通用 SSE 流式读取器 ====================
// 按 SSE 规范以空行分隔事件：一个事件内的多条 data: 行用 \n 拼接
// （Spring 会把含换行的内容拆成多条 data: 行，逐行 append 会丢失换行、破坏 Markdown）

function readSSEStream(response, { onStatus, onMessage, onDone, onError }) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let eventName = 'message'
  let dataLines = []
  let finished = false
  let errored = false

  const dispatch = () => {
    if (dataLines.length === 0) {
      eventName = 'message'
      return
    }
    const data = dataLines.join('\n')
    const name = eventName
    eventName = 'message'
    dataLines = []

    if (data.trim() === '[DONE]') {
      finished = true
      onDone && onDone()
      return
    }
    if (name === 'status') {
      onStatus && onStatus(data)
    } else if (name === 'error') {
      errored = true
      onError && onError(data)
    } else {
      onMessage && onMessage(data)
    }
  }

  const processLine = (rawLine) => {
    const line = rawLine.endsWith('\r') ? rawLine.slice(0, -1) : rawLine
    if (line === '') {
      dispatch()
      return
    }
    if (line.startsWith(':')) return
    const colon = line.indexOf(':')
    if (colon === -1) return
    const field = line.slice(0, colon)
    // Spring 的 ServerSentEvent 输出 "data:"（冒号后无空格），剥掉前导空格会把英文续词粘连
    const value = line.slice(colon + 1)
    if (field === 'event') {
      eventName = value
    } else if (field === 'data') {
      dataLines.push(value)
    }
  }

  const read = () => {
    reader.read().then(({ done, value }) => {
      if (done) {
        if (buffer) {
          processLine(buffer)
          buffer = ''
        }
        dispatch()
        if (!finished && !errored) onDone && onDone()
        return
      }
      buffer += decoder.decode(value, { stream: true })
      let index
      while ((index = buffer.indexOf('\n')) >= 0) {
        processLine(buffer.slice(0, index))
        buffer = buffer.slice(index + 1)
        if (finished || errored) {
          reader.cancel().catch(() => {})
          return
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
  return fetchWithAuth('/ai/admin/chat/sessions').then(res => res.json())
}

export const getAdminAiChatHistory = (sessionId, limit = 100) => {
  return fetchWithAuth(`/ai/admin/chat/history/${sessionId}?limit=${limit}`).then(res => res.json())
}

export const deleteAdminAiSession = (sessionId) => {
  return fetchWithAuth(`/ai/admin/chat/sessions/${sessionId}`, { method: 'DELETE' }).then(res => res.json())
}

export const chatStream = ({ message, context, sessionId, onStatus, onMessage, onDone, onError, signal }) => {
  if (typeof onMessage !== 'function') {
    throw new Error('chatStream: onMessage 回调函数为必填参数')
  }

  return fetchWithAuth('/ai/admin/chat', {
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
  return fetchWithAuth('/ai/chat/sessions').then(res => res.json())
}

export const getTeacherAiChatHistory = (sessionId, limit = 100) => {
  return fetchWithAuth(`/ai/chat/history/${sessionId}?limit=${limit}`).then(res => res.json())
}

export const deleteTeacherAiSession = (sessionId) => {
  return fetchWithAuth(`/ai/chat/sessions/${sessionId}`, { method: 'DELETE' }).then(res => res.json())
}

export const teacherChatStream = ({ message, context, sessionId, onStatus, onMessage, onDone, onError, signal }) => {
  if (typeof onMessage !== 'function') {
    throw new Error('teacherChatStream: onMessage 回调函数为必填参数')
  }

  return fetchWithAuth('/ai/teacher/chat', {
    method: 'POST',
    body: JSON.stringify({ message, context, sessionId }),
    signal
  }).then(response => {
    readSSEStream(response, { onStatus, onMessage, onDone, onError })
  }).catch(err => {
    onError && onError(err)
  })
}