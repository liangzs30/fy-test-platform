import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/elementCategory',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: 'api/elementCategory/',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: 'api/elementCategory',
    method: 'put',
    data
  })
}

export function getCategorys(params) {
  return request({
    url: '/api/elementCategory',
    method: 'get',
    params
  })
}

export function getCategorySuperior(ids, exclude) {
  exclude = exclude !== undefined ? exclude : false
  const data = ids.length || ids.length === 0 ? ids : Array.of(ids)
  return request({
    url: '/api/elementCategory/superior?exclude=' + exclude,
    method: 'post',
    data
  })
}

export default { add, edit, del, getCategorys, getCategorySuperior }
