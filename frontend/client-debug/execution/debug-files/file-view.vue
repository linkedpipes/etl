<template>
  <div style="margin-top: 1em">
    <div
      v-if="tooBig"
      class="text-xs-center"
      style="margin-left: 1em;margin-right: 1em"
    >
      <v-alert
        :value="true"
        type="warning"
      >
        This file may be too big for preview and may cause browser to crash.
      </v-alert>
      <v-btn @click="loadData">
        Show preview anyway
      </v-btn>
    </div>
    <div
      v-else-if="error"
      class="text-xs-center"
    >
      Can't load data.
    </div>
    <div
      v-else-if="loading"
      class="text-xs-center"
    >
      Loading ...
      <br>
      <br>
      <v-progress-circular
        :size="50"
        color="primary"
        indeterminate
      />
      <br>
    </div>
    <div
      v-else
      style="margin-left: 1rem;margin-right: 1rem;"
    >
      <pre><code style="width:100%;padding: 1rem;">{{ content }}</code></pre>
    </div>
    <br>
    <v-flex
      xs9
      sm6
      offset-sm3
    />
  </div>
</template>

<script>
  import Vue from "vue";
  import {fetchPlainText} from "@client-debug/app-service/http";
  import {getDownloadDebugUrl} from "./debug-files-service";

  const FILE_PREVIEW_LIMIT = 512 * 1024;

  export default Vue.extend({
    "name": "debug-files-file-view",
    "props": {
      "metadata": {"type": Object, "required": true},
      "query": {"type": Object, "required": true}
    },
    "data": () => ({
      "loading": false,
      "error": false,
      "content": undefined,
      "tooBig": false
    }),
    "mounted": function mounted() {
      if (this.metadata["size"] > FILE_PREVIEW_LIMIT) {
        this.tooBig = true;
        return;
      }
      this.loadData();
    },
    "methods": {
      "loadData": async function () {
        this.tooBig = false;
        try {
          this.loading = true;
          const response = await fetchPlainText(this.downloadUrl);
          this.content = response.text;
          this.loading = false;
          this.error = false;
        } catch (ex) {
          console.error("Can't download data", ex);
          this.error = true;
        }
      }
    },
    "computed": {
      "downloadUrl": function () {
        return getDownloadDebugUrl(
          this.$route.params["execution"],
          this.$route.query["path"],
          this.$route.query["source"]);
      }
    }
  });
</script>