<template>
  <v-flex
    xs9
    sm6
    offset-sm3
  >
    Number of records: {{ metadata.count }}<br>
    <v-list two-line>
      <template v-for="(item, index) in data">
        <list-item-dir
          v-show="item.type === 'dir'"
          :key="`dir-${index}`"
          :value="item"
          @onNavigateTo="onNavigateTo"
        />
        <list-item-file
          v-show="item.type === 'file'"
          :key="`file-${index}`"
          :value="item"
          @onNavigateTo="onNavigateTo"
        />
        <v-divider
          v-if="index + 1 < data.length"
          :key="`divider-${index}`"
        />
      </template>
    </v-list>
    <br>
    <v-layout row>
      <v-flex xs9>
        <v-pagination
          v-show="metadata.pageCount > 1"
          v-model="query.page"
          :length="metadata.pageCount"
          @input="onPageChange"
        />
      </v-flex>
      <v-flex xs2>
        <v-select
          v-model="query.pageSize"
          :items="[10, 50, 100]"
          label="Page size"
          @input="onPageSizeChange"
        />
      </v-flex>
    </v-layout>
  </v-flex>
</template>

<script>
  import Vue from "vue";
  import ListItemDir from "./list-item-dir";
  import ListItemFile from "./list-item-file";

  export default Vue.extend({
    "name": "debug-files-dir-view",
    "props": {
      "data": {"type": Array, "required": true},
      "metadata": {"type": Object, "required": true},
      "query": {"type": Object, "required": true}
    },
    "components": {
      "list-item-dir": ListItemDir,
      "list-item-file": ListItemFile
    },
    "data": () => ({}),
    "methods": {
      "onNavigateTo": function (item) {
        if (item.type === "file") {
          this.$router.push({
            "query": {
              "path": mergePath(this.query["path"], item.name),
              "source": item.source
            }
          });
        } else {
          this.$router.push({
            "query": {
              ...this.query,
              "page": 1,
              "path": mergePath(this.query["path"], item.name),
              "source": item.source
            }
          });
        }
      },
      "onPageChange": function (value) {
        this.$router.push({
          "query": {
            ...this.query,
            "page": value,
          }
        });
      },
      "onPageSizeChange": function (value) {
        this.$router.push({
          "query": {
            ...this.query,
            "pageSize": value,
          }
        });
      }
    }
  });

  function mergePath(left, right) {
    if (left.endsWith("/")) {
      return left + right;
    } else {
      return left + "/" + right;
    }
  }

</script>