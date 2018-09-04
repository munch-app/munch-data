function hasAccess(allowed, req) {
  if (allowed.access && process.env.NODE_ENV === 'production') {
    return (req.user && req.user.access || []).some(function (access) {
      return allowed.access.some(access);
    })
  }
  return true
}

function setup(service, allowedList) {
  const {Router} = require('express');
  const router = Router();

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

  allowedList.forEach(allowed => {
    router[allowed.method]('/api' + allowed.path, function (req, res, next) {
      if (!hasAccess(allowed, req)) {
        res.send(403)
        return
      }

      service.request({
        url: req.url.replace(/^\/api/, ''),
        params: req.query,
        method: allowed.method,
        data: req.body
      }).then(({data}) => {
        res.json(data);
      }).catch(next)
    });
  })
  return router
}

module.exports = {
  setup: setup
}
