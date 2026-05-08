import request from '@/utils/request'

export const getBookRecommend = () => request.get('/ai/user/book-recommend')
export const getHotBooks = () => request.get('/ai/user/hot-books')
export const getReadingAnalysis = () => request.get('/ai/user/reading-analysis')
export const getScoreAnalysis = (params) => request.get('/ai/user/score-analysis', { params })
export const getCourseRecommend = () => request.get('/ai/user/course-recommend')
