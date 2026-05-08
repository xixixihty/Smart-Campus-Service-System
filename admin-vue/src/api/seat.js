import request from '@/utils/request'

export const getSeatList = (params) => request.get('/seats/admin', { params })
export const getSeatDetail = (id) => request.get(`/seats/admin/${id}`)
export const createSeat = (data) => request.post('/seats/admin', data)
export const updateSeat = (data) => request.put('/seats/admin', data)
export const deleteSeat = (ids) => request.delete(`/seats/admin/${ids}`)
