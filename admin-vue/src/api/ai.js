import request from '@/utils/request'

export const getDashboardStats = () => request.get('/dashboard/admin/stats')

export const getAdminAiSessions = () => {
  return fetch('/api/ai/admin/chat/sessions', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
    return res.json()
  })
}

export const getAdminAiChatHistory = (sessionId, limit = 100) => {
  return fetch(`/api/ai/admin/chat/history/${sessionId}?limit=${limit}`, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
    return res.json()
  })
}

export const deleteAdminAiSession = (sessionId) => {
  return fetch(`/api/ai/admin/chat/sessions/${sessionId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
    return res.json()
  })
}

export const chatStream = ({ message, context, sessionId, onStatus, onMessage, onDone, onError, signal }) => {
  if (typeof onMessage !== 'function') {
    throw new Error('chatStream: onMessage 回调函数为必填参数')
  }

  const token = localStorage.getItem('token')
  return fetch('/api/ai/admin/chat', {
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