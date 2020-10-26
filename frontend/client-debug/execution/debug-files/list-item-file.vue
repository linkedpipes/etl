<template>
  <v-list-item
    :key="value.name"
    @click="$emit('onNavigateTo', value)"
  >
    <v-list-item-avatar>
      <v-icon>note</v-icon>
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title>
        {{ value.name }}
      </v-list-item-title>
      <v-list-item-subtitle>
        {{ asHumanReadableSize(value.size) }}
      </v-list-item-subtitle>
    </v-list-item-content>
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
  </v-list-item>
</template>

<script>
  import Vue from "vue";
  import {getDownloadDebugUrl} from "./debug-files-service";
  import {asHumanReadableSize} from "@client-debug/app-service/formats";

  export default Vue.extend({
    "name": "debug-files-list-item-file",
    "props": {
      "value": {"type": Object, "required": true}
    },
    "computed": {
      "downloadUrl": function () {
        let path = this.$route.query["path"] + "/" + this.value["name"];
        return getDownloadDebugUrl(
          this.value,
          this.$route.params["execution"],
          path,
          this.value["source"]);
      }
    },
    "methods": {
      "asHumanReadableSize": asHumanReadableSize
    }
  });
</script>