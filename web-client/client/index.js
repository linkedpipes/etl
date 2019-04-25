import Vue from "vue";
import Vuetify from "vuetify";
// Modules first.
import "./modules";
import App from "@/app/app.vue";
import router from "@/app/router";
import store from "@/app/store";

import "vuetify/dist/vuetify.css";

Vue.config.productionTip = true;
Vue.use(Vuetify);

/* eslint-disable no-new */
new Vue({
  "el": "#app",
  "store": store,
  "router": router,
  "render": (h) => h(App)
});
