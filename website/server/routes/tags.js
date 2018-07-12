const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.baseUrl
});

app.all('/api/tags/:tagId', function(req, res, next){
  return service.request({
    url: '/tags/' + req.params.tagId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

app.all('/api/tags', function(req, res, next){
  return service.request({
    url: '/tags',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

module.exports = router;
