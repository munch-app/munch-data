const catalyst = require('./base.js')
const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

router = catalyst.setup(service, [
  {method: 'get', path: '/brands'},
  {method: 'get', path: '/brands/:brandId'},

  {method: 'post', path: '/brands'},
  {method: 'put', path: '/brands/:brandId'},
  {method: 'delete', path: '/brands/:brandId'},
])

router.post('/api/search/brands', function (req, res, next) {
  console.log()
  service.request({
    url: '/elastic/search',
    params: req.query,
    method: 'post',
    data: req.body
  }).then(({data}) => {
    const brands = data &&
      data.data &&
      data.data.hits &&
      data.data.hits.hits &&
      data.data.hits.hits.map(hit => hit['_source'])
    res.json({
      meta: data.meta,
      data: brands
    });
  }).catch(next)
});

module.exports = router;
