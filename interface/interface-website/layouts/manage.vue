<template>
  <div>
    <nav-header></nav-header>
    <b-container>
      <b-breadcrumb class="Breadcrumb" :items="$store.state.breadcrumbs"/>
    </b-container>
    <nuxt/>
    <nav-bottom></nav-bottom>
  </div>
</template>

<script>
  import NavHeader from '~/components/NavHeader.vue'
  import NavBottom from '~/components/NavBottom.vue'

  export default {
    components: {
      NavHeader,
      NavBottom
    }
  }
</script>

<style lang="less">
  body {
    font-family: 'Open Sans', sans-serif;
  }

  .Breadcrumb {
    margin-top: 12px;
    margin-bottom: 12px;
  }
</style>
