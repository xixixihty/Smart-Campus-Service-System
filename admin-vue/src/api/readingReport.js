/**
 * 读书报告 API
 */
import request from '@/utils/request'

export function createReadingReport(data) {
  return request({ url: '/reading-reports/user', method: 'post', data })
}

export function getReadingReportList(params) {
  return request({ url: '/reading-reports/user', method: 'get', params })
}

export function getMyReadingReports(params) {
  return request({ url: '/reading-reports/user/my', method: 'get', params })
}

export function getReadingReportDetail(id) {
  return request({ url: `/reading-reports/user/${id}`, method: 'get' })
}