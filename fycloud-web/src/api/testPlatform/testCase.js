import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/caseCenter',
    method: 'post',
    data
  })
}

export function copy(data) {
  data.id = null
  return request({
    url: 'api/caseCenter',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: 'api/caseCenter/',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: 'api/caseCenter',
    method: 'put',
    data
  })
}

export function getCases(params) {
  return request({
    url: '/api/caseCenter',
    method: 'get',
    params
  })
}

export default { add, copy, edit, del, getCases }
