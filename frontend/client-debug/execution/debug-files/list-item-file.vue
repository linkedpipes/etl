<template>
  <v-list-tile
    :key="value.name"
    @click="$emit('onNavigateTo', value)"
  >
    <v-list-tile-avatar>
      <v-icon>note</v-icon>
    </v-list-tile-avatar>
    <v-list-tile-content>
      <v-list-tile-title>
        {{ value.name }}
      </v-list-tile-title>
      <v-list-tile-sub-title>
        {{ value.size }} bytes
      </v-list-tile-sub-title>
    </v-list-tile-content>
    <v-btn
      :href="downloadUrl"
      target="_blank"
      download
      icon
      ripple
      @click.stop="() => {}"
    >
      <v-icon>cloud_download</v-icon>
    </v-btn>
  </v-list-tile>
</template>

<script>
  import Vue from "vue";
  import {getDownloadDebugUrl} from "./debug-files-service";

  export default Vue.extend({
    "name": "debug-files-list-item-file",
    "props": {
      "value": {"type": Object, "required": true}
    },
    "computed": {
      "downloadUrl": function () {
        let path = this.$route.query["path"] + "/" + this.value["name"];
        return getDownloadDebugUrl(
          this.$route.params["execution"],
          path,
          this.value["source"]);
      }
    }
  });
</script>