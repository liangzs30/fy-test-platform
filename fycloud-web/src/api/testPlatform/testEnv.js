import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/testEnv',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: 'api/testEnv',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: 'api/testEnv',
    method: 'put',
    data
  })
}

export function getTestEnv(params) {
  return request({
    url: '/api/testEnv',
    method: 'get',
    params
  })
}



export default { add, edit, del, getTestEnv }
