import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/executeMachine',
    method: 'post',
    data
  })
}

export function del(ids) {
  return request({
    url: 'api/executeMachine',
    method: 'delete',
    data: ids
  })
}

export function getMachines(params) {
  return request({
    url: '/api/executeMachine',
    method: 'get',
    params
  })
}

export function getMachineTask(params) {
  return request({
    url: '/api/executeMachine/futrue/tasks',
    method: 'get',
    params
  })
}

export function cancelTaskById(id) {
  return request({
    url: '/api/executeMachine/task/cancel/' + id,
    method: 'delete',
  })
}

export function cancelAllTask(ids) {
  return request({
    url: '/api/executeMachine/task/cancelAll',
    method: 'delete',
    data: ids
  })
}

export function stopTask(data) {
  return request({
    url: '/api/executeMachine/task/stop',
    method: 'post',
    data: data
  })
}

export default { add, del, getMachines, getMachineTask, cancelTaskById, cancelAllTask, stopTask }
