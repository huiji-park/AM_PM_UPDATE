import { createStore } from 'vuex';

export default createStore({
  state() {
    return {
      etcPages: []
    };
  },
  mutations: {
    setEtcPages(state, pages) {
      state.etcPages = pages;
    }
  },
  actions: {
    fetchEtcPages({ commit }) {
      axios.get('../components').then(response => commit('setEtcPages', response.data));
    }
  }
});