const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.baseUrl
})

app.all('/api/areas/:areaId', function(req, res, next){
  return service.request({
    url: '/areas/' + req.params.areaId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

app.all('/api/areas', function(req, res, next){
  return service.request({
    url: '/areas',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

module.exports = router;
