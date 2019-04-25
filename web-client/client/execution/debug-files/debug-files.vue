<template>
  <v-content style="margin-bottom: 1rem">
    <v-flex xs9 sm6 offset-sm3>
      <div>
        Debug data for execution: {{ this.$route.params["execution"] }}<br/>
        Path: {{ this.query.path }}<br/>
        <br/>
      </div>
    </v-flex>
    <directory-view
      :metadata="metadata"
      :data="data"
      :query="query"
      v-show="metadata.type === 'dir'"
    />
    <file-view
      :metadata="metadata"
      :query="query"
      v-if="metadata.type === 'file'"
    />
    <ambiguous-view
      :data="data"
      :metadata="metadata"
      :query="query"
      v-if="metadata.type === 'ambiguous'"
    />
  </v-content>
</template>

<script>
  import Vue from "vue";
  import {fetchDebugMetadata} from "./debug-files-service";
  import DirectoryView from "./dir-view";
  import FileView from "./file-view";
  import AmbiguousView from "./ambiguous-view";

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
            (this.metadata.count || 0) / this.query.pageSize)
        };
        this.data = responseData.data || [];
      }
    }
  });

</script>