const catalyst = require('./base.js')
const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

module.exports = catalyst.setup(service, [
  {method: 'get', path: '/landmarks'},
  {method: 'get', path: '/landmarks/:landmarkId'},

  {method: 'post', path: '/landmarks'},
  {method: 'put', path: '/landmarks/:landmarkId'},
  {method: 'delete', path: '/landmarks/:landmarkId'},
])
