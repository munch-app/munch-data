module.exports = {
  /*
  ** Headers of the page
  */
  head: {
    title: 'Munch Data',
    meta: [
      {charset: 'utf-8'},
      {name: 'viewport', content: 'width=device-width, initial-scale=1'},
      {hid: 'description', name: 'description', content: 'Munch Data'}
    ],
    link: [
      {rel: 'icon', type: 'image/x-icon', href: '/favicon.ico'},
      {rel: 'stylesheet', href: 'https://fonts.googleapis.com/css?family=Open+Sans'}
    ]
  },
  css: [
    '~/assets/global.less'
  ],
  loading: {color: '#3B8070'},
  build: {
    /*
    ** Run ESLint on save
    */
    extend(config, {isDev, isClient}) {
      if (isDev && isClient) {
        config.module.rules.push({
          enforce: 'pre',
          test: /\.(js|vue)$/,
          loader: 'eslint-loader',
          exclude: /(node_modules)/
        })
      }
    }
  },
  modules: [
    'bootstrap-vue/nuxt',
    '@nuxtjs/axios',
  ],
  axios: {
    // proxyHeaders: false
  },
  serverMiddleware: [
    {
      path: '/_health', handler: function (req, res, next) {
        res.end('ok')
      }
    },
    // API middleware
    '~/server/index.js'
  ],
};
