import request from '@/utils/request'

export const getPeriodList = (params) => request.get('/course-selection-periods/admin', { params })
export const getPeriodDetail = (id) => request.get(`/course-selection-periods/admin/${id}`)
export const createPeriod = (data) => request.post('/course-selection-periods/admin', data)
export const updatePeriod = (data) => request.put('/course-selection-periods/admin', data)
export const deletePeriod = (ids) => request.delete(`/course-selection-periods/admin/${ids}`)
