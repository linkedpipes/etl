<template>
  <div>
    <div style="margin-left: 1rem;margin-right: 1rem;">
      <div v-if="tooBig">
        File is too big {{ metadata.size }} to show it as a preview.
      </div>
      <div v-else-if="error">
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
      <div v-else>
        <pre><code style="width:100%;padding: 1rem;">{{ content }}</code></pre>
      </div>
      <br>
    </div>
    <v-flex
      xs9
      sm6
      offset-sm3
    >
      <v-btn
        :href="downloadUrl"
        target="_blank"
        style="float:right;"
      >
        Open in new tab / Download
      </v-btn>
    </v-flex>
  </div>
</template>

<script>
  import Vue from "vue";
  import {fetchPlainText} from "@client-debug/app-service/http";
  import {getDownloadDebugUrl} from "./debug-files-service";

  const FILE_PREVIEW_LIMIT = 256 * 1024;

  export default Vue.extend({
    "name": "debug-files-file-view",
    "props": {
      "metadata": {"type": Object, "required": true},
      "query": {"type": Object, "required": true}
    },
    "data": () => ({
      "loading": false,
      "error": false,
      "tooBig": false,
      "content": undefined
    }),
    "mounted": function mounted() {
      this.loadData();
    },
    "methods": {
      "loadData": async function () {
        if (this.metadata["size"] > FILE_PREVIEW_LIMIT) {
          this.tooBig = true;
          return;
        }
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