import axios from 'axios';

const service = axios.create({
  baseURL: process.env.TASK_SERVICE_URL || 'http://localhost:8888/v1.0'
});

// All Returns Promises
export default {
  // Count Task: UserId, [TaskName]
  count: function (userId, taskNames) {
    return service.request({
      url: '/users/' + userId + '/tasks/count',
      method: 'post',
      data: taskNames
    });
  },

  // Query Task: UserId, TaskName
  get: function (userId, taskName) {
    return service.request({
      url: '/users/' + userId + '/tasks/' + taskName,
      method: 'get',
      validateStatus: function (status) {
        return status === 200 || status === 404;
      }
    });
  },

  // Close Task: UserId, TaskName, TaskId, Reply
  close: function (userId, taskName, taskId, reply) {
    return service.request({
      url: '/users/' + userId + '/tasks/' + taskName + '/' + taskId + '/close',
      method: 'patch',
      data: {
        reply: reply
      }
    });
  }
};
