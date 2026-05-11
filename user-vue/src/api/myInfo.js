import request from '@/utils/request'

export const getMyInfo = () => request.get('/my-infos')

export const updatePassword = (data) => request.put('/my-infos/password', data)
