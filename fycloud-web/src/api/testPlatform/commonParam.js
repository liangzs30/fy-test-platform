import request from '@/utils/request'

export function add(data) {
  return request({
    url: '/api/commonParam',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: '/api/commonParam',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: '/api/commonParam',
    method: 'put',
    data
  })
}

export function getCommonParam(params) {
  return request({
    url: '/api/commonParam',
    method: 'get',
    params
  })
}



export default { add, edit, del, getCommonParam }
