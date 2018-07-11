<template>
  <div class="DataGroupCard">

    <div class="TaskBorder d-flex align-items-stretch">
      <div class="d-flex align-items-stretch flex-column">
        <h4>{{name}}</h4>
        <p>{{description}}</p>
        <div class="mt-auto right">
          <b-button class="float-right" :href="path" variant="primary">Manage</b-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    props: ['name', 'description', 'path'],
    name: "DataTypeBox"
  }
</script>

<style lang="less">
  .DataGroupCard {
    margin-top: 10px;
    margin-bottom: 10px;
    .LabelCount {
      font-size: 85%;
    }

    div.TaskBorder {
      border: 1px solid rgba(0, 0, 0, .125);
      padding: 25px 20px 25px 20px;
      border-radius: 0.25em;
    }
  }
</style>
