<template>
  <div>
    <b-container class="Dashboard">
      <b-row>
        <b-col cols="12" md="4" v-for="task in typeList" :key="task.path" class="d-flex align-items-stretch">
          <data-type-box v-bind="task" class="d-flex align-items-stretch"></data-type-box>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
  import DataGroupBox from "../components/DataTypeBox";

  let typeList = [
    {
      name: "Brand",
      description: "Brand data",
      path: "brands",
    }
  ];

  export default {
    components: {DataGroupBox},
    data() {
      return {
        typeList: typeList
      }
    }
  }
</script>

<style lang="less">
  .Dashboard {
    margin-top: 40px;
    margin-bottom: 40px;
  }
</style>
