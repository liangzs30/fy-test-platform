import request from '@/utils/request'

export function add(data) {
  return request({
    url: '/api/web/element',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: '/api/web/element',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: '/api/web/element',
    method: 'put',
    data
  })
}

export function getElements(params) {
  return request({
    url: '/api/web/element',
    method: 'get',
    params
  })
}

export function getElement(id) {
  return request({
    url: '/api/web/element/get/' + id,
    method: 'get',
  })
}

export default { add, edit, del, getElements, getElement }
