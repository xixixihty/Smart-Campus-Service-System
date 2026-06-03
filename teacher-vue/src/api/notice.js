import request from '@/utils/request'

// 我的通知列表
export const getMyNotices = (params) => request.get('/teacher/notices', { params })

// 通知详情
export const getNoticeDetail = (id) => request.get(`/teacher/notice/${id}`)

// 未读通知数
export const getUnreadCount = () => request.get('/teacher/notice/unread-count')