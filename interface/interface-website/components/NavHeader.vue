<template>
  <b-navbar toggleable="md" variant="light">
    <div class="container">

      <b-navbar-brand href="/">
        <div class="MunchLogo">Munch Data</div>
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
  }
</script>

<style scoped lang="less">
  .MunchLogo {
    font-weight: 900;
    font-size: 1.3rem;
  }
</style>
