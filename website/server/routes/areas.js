const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

router.all('/api/areas/:areaId', function(req, res, next){
  service.request({
    url: '/areas/' + req.params.areaId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

router.all('/api/areas', function(req, res, next){
  service.request({
    url: '/areas',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

module.exports = router;
