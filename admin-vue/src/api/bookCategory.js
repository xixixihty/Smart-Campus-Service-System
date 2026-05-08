import request from '@/utils/request'

export const getCategoryList = (params) => request.get('/book-categories/admin', { params })
export const getCategoryDetail = (id) => request.get(`/book-categories/admin/${id}`)
export const createCategory = (data) => request.post('/book-categories/admin', data)
export const updateCategory = (data) => request.put('/book-categories/admin', data)
export const deleteCategory = (ids) => request.delete(`/book-categories/admin/${ids}`)
