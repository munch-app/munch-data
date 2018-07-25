const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
});

service.interceptors.response.use(function (response) {
  return response;
}, function (error) {
  let message = error.response &&
    error.response.data &&
    error.response.data.meta &&
    error.response.data.meta.error &&
    error.response.data.meta.error.message
  if (message) {
    return Promise.reject(new Error(message))
  }
  return Promise.reject(error);
});


router.all('/api/tags/:tagId', function (req, res, next) {
  service.request({
    url: '/tags/' + req.params.tagId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

router.all('/api/tags', function (req, res, next) {
  service.request({
    url: '/tags',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

module.exports = router;
