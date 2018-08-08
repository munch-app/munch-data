<template>
  <div>
    <b-container class="Dashboard">
      <b-row>
        <b-col cols="12" md="6" v-for="task in typeList" :key="task.path" class="d-flex align-items-stretch">
          <data-group-box v-bind="task" class="d-flex align-items-stretch"></data-group-box>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
  import DataGroupBox from "../components/DataTypeBox";

  let typeList = [
    // {
    //   name: "Tag",
    //   description: "Tag is used to tag places with Food, Cuisine, Establishment, Amenities and Timing tags.",
    //   path: "tags",
    // },
    {
      name: "Brand",
      description: "Brand is a Franchise/Brand entity that usually encompass multiple Place data.",
      path: "brands",
    },
    // {
    //   name: "Landmark",
    //   description: "Landmark is used for location context purpose to showcase how close a place is to a known Landmark.",
    //   path: "landmarks",
    // },
    // {
    //   name: "Area",
    //   description: "Area is a polygon in a country. Area can be a city, region or a location cluster. Polygon is required for all rea.",
    //   path: "areas",
    // }
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

<style scoped lang="less">
  .Dashboard {
    margin-top: 24px;
    margin-bottom: 24px;
  }
</style>
