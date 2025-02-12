import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/kw',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: 'api/kw/',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: 'api/kw',
    method: 'put',
    data
  })
}

export function getKws(params) {
  return request({
    url: '/api/kw',
    method: 'get',
    params
  })
}

export default { add, edit, del, getKws }
