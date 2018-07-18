const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

router.all('/api/brands/:brandId', function (req, res, next) {
  service.request({
    url: '/brands/' + req.params.brandId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

router.all('/api/brands', function (req, res, next) {
  service.request({
    url: '/brands',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

module.exports = router;
