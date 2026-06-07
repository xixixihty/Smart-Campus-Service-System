/**
 * 教师端 AI 助手 API
 */

const BASE_URL = '/api/ai/teacher'

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