import Vue from "vue";
import Vuex from "vuex";
import {getRegistered} from "./registry"

Vue.use(Vuex);

const modules = {};
getRegistered().forEach((module) => {
  if (module["store"] && module["name"]) {
    modules[module["name"]] = module["store"];
  }
});

const store = new Vuex.Store({
  "modules": modules
});

export default store;
