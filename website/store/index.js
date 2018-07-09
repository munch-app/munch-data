export const state = () => ({
  user: {userId: 'testuserid', name: 'Test Name', email: 'test@munch.space'}
});

export const mutations = {
  SET_USER: function (state, user) {
    state.user = user
  }
};

export const actions = {
  // nuxtServerInit is called by Nuxt.js before server-rendering every page
  nuxtServerInit({commit}, {req}) {
    if (req.user) {
      commit('SET_USER', req.user)
    }
  },

  logout({commit}) {
    commit('SET_USER', null)
  }
};
