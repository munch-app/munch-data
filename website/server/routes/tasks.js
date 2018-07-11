const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.TASK_SERVICE_URL || 'http://localhost:8888/v1.0'
});

router.post('/api/tasks/:taskName/:taskId/close', function (req, res, next) {
  let userId = 'testuserid';
  if (req.user && req.user.userId) {
    userId = req.user.userId;
  }

  return service.request({
    url: '/users/' + userId + '/tasks/' + req.params.taskName + '/' + req.params.taskId + '/close',
    method: 'patch',
    data: req.body
  }).then((ress) => {
    res.json(ress.data);
  });
});

router.get('/api/tasks/:taskName/closed/list', function (req, res, next) {
  return service.request({
    url: '/tasks/' + req.params.taskName + '/closed/list',
    params: {
      from: req.query.from,
      size: req.query.size
    },
    method: 'get',
    data: req.body
  }).then((ress) => {
    res.json(ress.data);
  });
});

module.exports = router;
