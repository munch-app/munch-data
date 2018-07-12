const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.DATA_SERVICE_URL || 'http://localhost:8052/v4.0'
});

app.get('/api/elastic/search', function (req, res, next) {
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
  });
});

module.exports = router;
