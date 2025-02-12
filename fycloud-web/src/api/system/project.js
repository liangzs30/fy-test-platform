import request from '@/utils/request'

// 获取所有的Project
export function getAll() {
  return request({
    url: 'api/projects/all',
    method: 'get'
  })
}

export function add(data) {
  return request({
    url: 'api/projects',
    method: 'post',
    data
  })
}

export function get(id) {
  return request({
    url: 'api/projects/' + id,
    method: 'get'
  })
}

export function del(ids) {
  return request({
    url: 'api/projects',
    method: 'delete',
    data: ids
  })
}

export function edit(data) {
  return request({
    url: 'api/projects',
    method: 'put',
    data
  })
}

export function getProjectsByUser(id) {
  return request({
    url: 'api/projects/user/projecets/' + id,
    method: 'get'
  })
}

export function queryUserProject(id) {
  return request({
    url: 'api/projects/user/projecet/' + id,
    method: 'get'
  })
}

export function saveLastProject(data) {
  return request({
    url: 'api/projects/user/projecet',
    method: 'put',
    data
  })
}

export default { add, edit, del, get, getProjectsByUser, queryUserProject, saveLastProject }
