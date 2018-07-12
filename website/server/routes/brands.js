const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.dataServiceUrl
})

app.all('/api/brands/:brandId', function(req, res, next){
  return service.request({
    url: '/brands/' + req.params.brandId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

app.all('/api/brands', function(req, res, next){
  return service.request({
    url: '/brands',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

module.exports = router;
