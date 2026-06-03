import request from '@/utils/request'

export const getUnreadNotificationCount = () => request.get('/notifications/unread-count')
export const markAllNotificationsAsRead = () => request.put('/notifications/read-all')