import request from '@/utils/request'

export function add(data) {
  return request({
    url: '/api/customer/function',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: '/api/customer/function',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: '/api/customer/function',
    method: 'put',
    data
  })
}

export function getFuncList(params) {
  return request({
    url: '/api/customer/function',
    method: 'get',
    params
  })
}

export default { add, edit, del, getFuncList }
