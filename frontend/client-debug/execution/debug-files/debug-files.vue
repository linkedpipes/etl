<template>
  <v-content style="margin-bottom: 1rem">
    <directory-view
      v-show="metadata.type === 'dir'"
      :metadata="metadata"
      :data="data"
      :query="query"
    />
    <file-view
      v-if="metadata.type === 'file'"
      :metadata="metadata"
      :query="query"
    />
    <ambiguous-view
      v-if="metadata.type === 'ambiguous'"
      :data="data"
      :metadata="metadata"
      :query="query"
    />
  </v-content>
</template>

<script>
  import Vue from "vue";
  import {fetchDebugMetadata} from "./debug-files-service";
  import DirectoryView from "./dir-view";
  import FileView from "./file-view";
  import AmbiguousView from "./ambiguous-view";
  import {onHome, onListing, onDetail} from "@client-debug/app/header-data"
  import {getDownloadDebugUrl} from "./debug-files-service";

  export default Vue.extend({
    "name": "debug-files",
    "components": {
      "directory-view": DirectoryView,
      "file-view": FileView,
      "ambiguous-view": AmbiguousView
    },
    "data": () => ({
      "metadata": {
        "count": undefined,
        "type": undefined,
        "pageCount": undefined
      },
      "data": [],
      "query": {
        "path": "",
        "page": undefined,
        "pageSize": undefined
      }
    }),
    "mounted": function mounted() {
      this.updateStateFromQuery();
      this.loadData();
    },
    "watch": {
      "$route": function () {
        // React to route changes... as the component may be reused.
        this.updateStateFromQuery();
        this.loadData();
      }
    },
    "methods": {
      "updateStateFromQuery": function () {
        this.query.path = this.$route.query["path"] || "/";
        this.query.source = this.$route.query["source"] || "";
        this.query.page = parseInt(this.$route.query["page"]) || 1;
        this.query.pageSize = parseInt(this.$route.query["pageSize"]) || 10;
      },
      "loadData": async function () {
        const response = await fetchDebugMetadata(
          this.$route.params["execution"],
          this.query.path,
          this.query.source,
          (this.query.page - 1) * this.query.pageSize,
          this.query.pageSize);
        const responseData = response.json;
        // As we keep both views active, we need to make sure that we have
        // some data.
        this.metadata = {
          ...responseData.metadata,
          "count": responseData.metadata["count"] || 0,
          "pageCount": Math.ceil(
            (responseData.metadata["count"] || 0) / this.query.pageSize)
        };
        this.data = responseData.data || [];
        // Update header.
        updateHeader(
          this.metadata,
          this.$route,
          this.query);
      }
    }
  });

  function updateHeader(metadata, route, query) {
    const execution = route.params["execution"];
    if (metadata.type === "dir") {
      onListing(execution, query.path, metadata.count);
    } else if (metadata.type === "file") {
      const downloadUrl = getDownloadDebugUrl(
        execution, query.path, route.query["source"]);
      onDetail(execution, query.path, metadata.size, downloadUrl)
    } else if (metadata.type === "ambiguous") {
      onListing(execution, query.path, metadata.count);
    } else {
      onHome();
    }
  }

</script>