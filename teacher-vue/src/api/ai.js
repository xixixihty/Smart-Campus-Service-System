/**
 * 教师端 AI 助手 API
 */

const BASE_URL = '/api/ai/teacher'

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

/**
 * 流式对话
 * @param {Object} options - 配置项
 * @param {string} options.message - 用户消息
 * @param {string} [options.context] - 附加上下文
 * @param {string} [options.sessionId] - 会话ID
 * @param {Function} options.onStatus - 状态回调
 * @param {Function} options.onMessage - 流式消息回调
 * @param {Function} options.onDone - 完成回调
 * @param {Function} options.onError - 错误回调
 * @param {AbortSignal} [options.signal] - 中断信号
 */
export const chatStream = ({ message, context, sessionId, onStatus, onMessage, onDone, onError, signal }) => {
  if (typeof onMessage !== 'function') {
    throw new Error('chatStream: onMessage 回调函数为必填参数')
  }

  const token = localStorage.getItem('token')
  return fetch(`${BASE_URL}/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify({ message, context, sessionId }),
    signal
  }).then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    readSSEStream(response, { onStatus, onMessage, onDone, onError })
  }).catch(err => {
    onError && onError(err)
  })
}

/**
 * 获取 AI 对话会话列表
 */
export const getAiSessions = () => {
  return fetch('/api/ai/chat/sessions', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
    return res.json()
  })
}

/**
 * 获取 AI 对话历史记录
 * @param {string} sessionId - 会话ID
 * @param {number} [limit=100] - 返回条数
 */
export const getAiChatHistory = (sessionId, limit = 100) => {
  return fetch(`/api/ai/chat/history/${sessionId}?limit=${limit}`, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
    return res.json()
  })
}

/**
 * 删除 AI 对话会话
 * @param {string} sessionId - 会话ID
 */
export const deleteAiSession = (sessionId) => {
  return fetch(`/api/ai/chat/sessions/${sessionId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
    return res.json()
  })
}