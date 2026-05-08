import request from '@/utils/request'

export const getBookList = (params) => request.get('/books/admin', { params })
export const getBookDetail = (id) => request.get(`/books/admin/${id}`)
export const createBook = (data) => request.post('/books/admin', data)
export const updateBook = (data) => request.put('/books/admin', data)
export const deleteBook = (ids) => request.delete(`/books/admin/${ids}`)
