import request from '@/utils/request'
import axios from 'axios'

const aiRequest = axios.create({
  baseURL: '/api',
  timeout: 120000
})

aiRequest.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

aiRequest.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === '200' || res.code === 200) {
      return res
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => Promise.reject(error)
)

export const getCampusOverview = () => aiRequest.get('/ai/admin/campus-overview')
export const getTeachingQuality = (params) => aiRequest.get('/ai/admin/teaching-quality', { params })
export const getScoreAnalysis = (params) => aiRequest.get('/ai/admin/score-analysis', { params })
export const getResourceOptimization = () => aiRequest.get('/ai/admin/resource-optimization')
export const getDashboardStats = () => request.get('/dashboard/admin/stats')

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
