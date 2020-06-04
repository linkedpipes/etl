import Vue from "vue";
import Vuetify from "vuetify";
import VueRouter from "vue-router";
// Modules first.
import "./modules";
import App from "./app/app.vue";
import router from "./app/router";
import store from "./app/store";

import "vuetify/dist/vuetify.css";

Vue.config.productionTip = true;
Vue.use(Vuetify);
Vue.use(VueRouter);

const vuetify = new Vuetify({});

/* eslint-disable no-new */
new Vue({
  "el": "#app",
  "vuetify": vuetify,
  "store": store,
  "router": router,
  "render": (h) => h(App)
});