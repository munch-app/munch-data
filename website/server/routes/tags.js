const catalyst = require('./base.js')
const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

router = catalyst.setup(service, [
  {method: 'get', path: '/tags'},
  {method: 'get', path: '/tags/:tagId'},

  {method: 'post', path: '/tags'},
  {method: 'put', path: '/tags/:tagId'},
  {method: 'delete', path: '/tags/:tagId'},
  {method: 'patch', path: '/tags/:tagId'},
])

let cached = []

router.get('/api/cached/tags', function (req, res, next) {
  if (!cached.isEmpty) {
    res.json({meta: {code: 200}, data: cached})
    return
  }

  service.request({
    url: '/tags',
    params: {'size': 500},
    method: 'get'
  }).then(({data}) => {
    cached = data.data
    res.json(data);
  }).catch(next)
})

module.exports = router
