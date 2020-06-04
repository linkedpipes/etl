<template>
  <v-app-bar
    color="green"
    dark
    app
  >
    <v-btn
      icon
      @click="onBack"
    >
      <v-icon>
        arrow_back
      </v-icon>
    </v-btn>
    <v-toolbar-title>{{ title }}</v-toolbar-title>
    <v-spacer />
    <v-btn
      v-show="downloadUrl"
      :href="downloadUrl"
      target="_blank"
      download
      icon
    >
      <v-icon>
        cloud_download
      </v-icon>
    </v-btn>
  </v-app-bar>
</template>

<script>
  import Vue from "vue";
  import {getData} from "./header-data";

  export default Vue.extend({
    "name": "app-header",
    "data": getData,
    "methods": {
      "onBack": function () {
        this.$router.go(-1);
      }
    }
  });
</script>