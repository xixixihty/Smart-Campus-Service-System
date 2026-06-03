import request from '@/utils/request'

// 获取个人信息（复用 admin 的 myInfo API）
export const getMyInfo = () => request.get('/my-info')

// 修改密码
export const updatePassword = (data) => request.put('/my-info/password', data)