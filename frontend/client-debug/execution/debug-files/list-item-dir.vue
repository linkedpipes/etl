<template>
  <v-list-item
    :key="value.name"
    @click="$emit('onNavigateTo', value)"
  >
    <v-list-item-avatar>
      <v-icon>folder</v-icon>
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title>
        {{ value.name }}
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
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