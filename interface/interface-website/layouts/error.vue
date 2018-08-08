<template>
  <div class="container Error">
    <b-row>
      <b-col cols="12">
        <h1 v-if="error.statusCode === 404">Page Not Found</h1>
        <h1 v-else>An error occurred</h1>

        <nuxt-link to="/">Home Page</nuxt-link>
      </b-col>
    </b-row>
  </div>
</template>

<script>
  export default {
    props: ['error']
  }
</script>

<style>
  .Error {
    margin-top: 50px;
  }
</style>
