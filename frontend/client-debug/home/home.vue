<template>
  <div />
</template>

<script>
  import Vue from "vue";
  import {onHome} from "@client-debug/app/header-data"

  export default Vue.extend({
    "name": "home",
    "mounted": function () {
      onHome();
    }
  });
</script>