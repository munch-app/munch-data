<template>
  <div>
    <table class="Brands table">
      <thead>
      <tr>
        <th scope="col">Names</th>
        <th scope="col">Tags</th>
        <th scope="col">Description</th>
        <th scope="col">Price</th>
        <th scope="col">Menu</th>
        <th scope="col"></th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="brand in results" :key="brand.tagId">
        <td>
          {{brand.name}} ({{brand.names.toString()}})
        </td>
        <td>
          {{brand.tags.map(a=>a.name).toString()}}
        </td>
        <td>
          {{brand.description}}
        </td>
        <td>
          {{brand.price.perPax}}
        </td>
        <td>
          {{brand.menu.url}}
        </td>
        <td>
          <b-button class="Button" @click="$router.push({path: `/brands/${brand.brandId}`})" variant="outline-primary">Edit</b-button>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
  export default {
    name: "BrandsList",
    props: {
      results: {
        type: Array,
        required: true
      },
    },
    data() {
      return {
        detailed: false,
        expended: false,
      }
    },
  }
</script>

<style scoped lang="less">
  .Brands {
    margin-top: 15px;
  }
</style>
