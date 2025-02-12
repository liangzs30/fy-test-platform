import request from '@/utils/request'

export function getReports(params) {
  return request({
    url: '/api/testReport',
    method: 'get',
    params
  })
}

export function getReportDetail(id) {
  return request({
    url: '/api/testReport/detail/' + id,
    method: 'get'
  })
}

export default { getReports, getReportDetail }
