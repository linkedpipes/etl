<template>
  <v-toolbar
    color="green"
    dark
    fixed
    app
  >
    <v-btn flat>
      <v-icon @click="onBack">
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
      flat
    >
      <v-icon>
        open_in_new
      </v-icon>
    </v-btn>
  </v-toolbar>
</template>

<script>
  import Vue from "vue";
  import {getData} from "./header-data";

  export default Vue.extend({
    "name": "app-header",
    "data": getData,
    "watch": {
      "$route": function () {
        console.log(this.$router.history);
      }
    },
    "methods": {
      "onBack": function () {
        this.$router.go(-1);
      }
    }
  });
</script>