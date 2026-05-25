import request from '@/utils/request'

export const reserveSeat = (data) => request.post('/seat-reservations/user', data)
export const cancelReservation = (id) => request.put(`/seat-reservations/user/${id}/cancel`)
export const checkIn = (id) => request.put(`/seat-reservations/user/${id}/check-in`)
export const checkOut = (id) => request.put(`/seat-reservations/user/${id}/check-out`)
export const leaveSeat = (id) => request.put(`/seat-reservations/user/${id}/leave`)
export const getReservationList = (params) => request.get('/seat-reservations/user', { params })
export const getReservationDetail = (id) => request.get(`/seat-reservations/user/detail/${id}`)
