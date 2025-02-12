import request from '@/utils/request'

export function add(data) {
  return request({
    url: '/api/mailConf/add',
    method: 'post',
    data
  })
}

export function del(id) {
  return request({
    url: '/api/mailConf/delete' + id,
    method: 'delete'
  })
}

export function edit(data) {
  return request({
    url: '/api/mailConf/update',
    method: 'put',
    data
  })
}

export function getConf(pid) {
  return request({
    url: '/api/mailConf/' + pid,
    method: 'get'
  })
}



export default { add, edit, del, getConf }
