const catalyst = require('./base.js')
const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

module.exports = catalyst.setup(service, [
  {method: 'get', path: '/areas'},
  {method: 'get', path: '/areas/:areaId'},
  {method: 'get', path: '/areas/:areaId/count/places'},

  {method: 'post', path: '/areas'},
  {method: 'put', path: '/areas/:areaId'},
  {method: 'delete', path: '/areas/:areaId'},
])
