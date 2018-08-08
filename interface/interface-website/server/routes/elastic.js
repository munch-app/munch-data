const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL
})

service.interceptors.response.use(function (response) {
  return response;
}, function (error) {
  let message = error.response &&
    error.response.data &&
    error.response.data.meta &&
    error.response.data.meta.error &&
    error.response.data.meta.error.message
  if (message) {
    return Promise.reject(new Error(message))
  }
  return Promise.reject(error);
});


router.get('/api/elastic/search', function (req, res, next) {
  let data = {
    from: req.query.from || 0,
    size: req.query.size || 20,
    query: {
      bool: {
        filter: [],
        must: {}
      }
    }
  }

  // Type filtering
  if (req.query.type) {
    data.query.bool.filter = [{
      term: {
        dataType: req.query.type
      }
    }]
  }

  // Text querying
  if (req.query.text) {
    data.query.bool.must = {
      match: {
        name: req.query.text
      }
    }
  }

  service.request({
    url: '/elastic/search',
    method: 'post',
    data: data
  }).then(({data}) => {
    res.json(data.hits.hits);
  }).catch(next)
});

module.exports = router;
