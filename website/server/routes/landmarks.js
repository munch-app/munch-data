const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

router.all('/api/landmarks/:landmarkId', function (req, res, next) {
  service.request({
    url: '/landmarks/' + req.params.landmarkId,
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

router.all('/api/landmarks', function (req, res, next) {
  service.request({
    url: '/landmarks',
    params: req.query,
    method: req.method,
    data: req.body
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

module.exports = router;
