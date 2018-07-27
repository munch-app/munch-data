const catalyst = require('./base.js')
const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

module.exports = catalyst.setup(service, [
  {method: 'get', path: '/brands'},
  {method: 'get', path: '/brands/:brandId'},

  {method: 'post', path: '/brands'},
  {method: 'put', path: '/brands/:brandId'},
  {method: 'delete', path: '/brands/:brandId'},
])
