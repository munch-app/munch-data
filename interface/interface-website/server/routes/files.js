const {Router} = require('express');
const router = Router();

const multer = require('multer')
const upload = multer({storage: multer.memoryStorage()})
const FormData = require('form-data');

const service = require('axios').create({
  baseURL: process.env.FILE_SERVICE_URL
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


router.post('/api/files/images/upload', upload.single('file'), function (req, res, next) {
  const form = new FormData();
  const file = req.file
  form.append('file', file.buffer, file.originalname);
  form.append('profile', '{"type": "munch-data"}')

  service.request({
    url: '/images/upload',
    headers: form.getHeaders(),
    method: 'post',
    data: form
  }).then(({data}) => {
    res.json(data);
  }).catch(next)
});

module.exports = router;
