const catalyst = require('./base.js')
const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

module.exports = catalyst.setup(service, [
  {method: 'get', path: '/tags'},
  {method: 'get', path: '/tags/:tagId'},

  {method: 'post', path: '/tags'},
  {method: 'put', path: '/tags/:tagId'},
  {method: 'delete', path: '/tags/:tagId'},
  {method: 'patch', path: '/tags/:tagId'},
])
