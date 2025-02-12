import request from '@/utils/request'

export function createRunTask(data) {
  return request({
    url: 'api/runTask',
    method: 'post',
    data
  })
}

export default { createRunTask }
