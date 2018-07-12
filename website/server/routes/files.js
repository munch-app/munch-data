const {Router} = require('express');
const router = Router();

const service = require('axios').create({
  baseURL: process.env.fileServiceUrl
})

app.post('/api/files/images/upload', function(req, res, next){
  if (!req.accepts('multipart/form-data')) {
    next()
  }

  return service.request({
    url: '/images/upload',
    method: 'post',
    data: req.body
  }).then(({data}) => {
    res.json(data);
  });
});

module.exports = router;
