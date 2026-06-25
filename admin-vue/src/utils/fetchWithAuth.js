/**
 * 带统一认证与错误处理的 fetch 封装
 * 用于 AI 等需要流式响应的场景（无法直接使用 axios）
 */
import { ElMessage } from 'element-plus'
import router from '@/router'

const BASE_URL = '/api'

/**
 * 发送带认证的 fetch 请求，自动注入 Token 并统一处理 401 错误
 * @param {string} url - API 路径（相对于 /api）
 * @param {object} options - fetch 配置项
 * @returns {Promise<Response>}
 */
export async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem('token')
  const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`

  const response = await fetch(fullUrl, {
    ...options,
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json',
      ...options.headers
    }
  })

  if (response.status === 401) {
    ElMessage.error('登录已过期，请重新登录')
    localStorage.clear()
    router.push('/login')
    throw new Error('Unauthorized')
  }

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  return response
}