import request from '@/utils/request'

export const getCurrentSemester = () => request.get('/semesters/admin/current')