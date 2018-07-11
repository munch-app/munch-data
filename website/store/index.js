export const state = () => ({
  user: {userId: 'testuserid', name: 'Test Name', email: 'test@munch.space'},
  breadcrumbs: []
});

export const mutations = {
  SET_USER: function (state, user) {
    state.user = user
  },
  SET_BREADCRUMBS: function (state, breadcrumbs) {
    state.breadcrumbs  = breadcrumbs
  }
};

export const actions = {
  // nuxtServerInit is called by Nuxt.js before server-rendering every page
  nuxtServerInit({commit}, {req}) {
    if (req.user) {
      commit('SET_USER', req.user)
    }

    let breadcrumbs = [{
      text: 'Dashboard',
      href: '/'
    }];
    let path = '/'

    for (let value of req.url.split('/')) {
      if (value !== '') {
        path += value
        value = value.charAt(0).toUpperCase() + value.slice(1);
        breadcrumbs.push({
          text: value,
          href: path
        })
      }
    }
    commit('SET_BREADCRUMBS', breadcrumbs)
  },

  logout({commit}) {
    commit('SET_USER', null)
  }
};
