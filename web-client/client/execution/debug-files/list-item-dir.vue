<template>
  <v-list-tile
    :key="value.name"
    @click="$emit('onNavigateTo', value)"
  >
    <v-list-tile-avatar>
      <v-icon>folder</v-icon>
    </v-list-tile-avatar>
    <v-list-tile-content>
      <v-list-tile-title>
        {{ value.name }}
      </v-list-tile-title>
    </v-list-tile-content>
  </v-list-tile>
</template>

<script>
  import Vue from "vue";

  export default Vue.extend({
    "name": "debug-files-list-item-dir",
    "props": {
      "value": {"type": Object, "required": true}
    }
  });
</script>