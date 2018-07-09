<template>
  <b-navbar toggleable="md" variant="light">
    <div class="container">

      <b-navbar-brand href="/">
        <h5 class="MunchLogo"><b>Munch HIT</b></h5>
      </b-navbar-brand>

      <b-navbar-nav class="ml-auto">
        <b-nav-item-dropdown :text="$store.state.user.name" right>
          <b-dropdown-item href="/logout">Logout</b-dropdown-item>
          <b-dropdown-item>Build 1.0</b-dropdown-item>
        </b-nav-item-dropdown>
      </b-navbar-nav>
    </div>
  </b-navbar>
</template>

<script>
  export default {
    // computed: {
    //   user() {
    //     return this.@store.state.user;
    //   }
    // }
  }
</script>

<style lang="less">

</style>
