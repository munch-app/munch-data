const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL || 'http://localhost:8052/v4.0'
});

app.all('/api/landmarks/:tagId', function(req, res, next){
  return service.request({
    url: '/landmarks/' + req.params.tagId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

app.all('/api/landmarks', function(req, res, next){
  return service.request({
    url: '/landmarks',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

module.exports = router;
