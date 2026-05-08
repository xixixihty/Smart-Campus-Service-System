import request from '@/utils/request'

export const getCampusOverview = () => request.get('/ai/admin/campus-overview')
export const getTeachingQuality = (params) => request.get('/ai/admin/teaching-quality', { params })
export const getScoreAnalysis = (params) => request.get('/ai/admin/score-analysis', { params })
export const getResourceOptimization = () => request.get('/ai/admin/resource-optimization')

export const chatStream = (message, context, onMessage, onDone, onError) => {
  const token = localStorage.getItem('token')
  return fetch('/api/ai/admin/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify({ message, context })
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
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data) {
              onMessage && onMessage(data)
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
