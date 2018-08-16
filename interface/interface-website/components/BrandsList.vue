<template>
  <div>
    <table class="Brands table">
      <thead>
      <tr>
        <th scope="col">Names</th>
        <th scope="col">Tags</th>
        <th scope="col">Price</th>
        <th scope="col"></th>
        <th scope="col"></th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="brand in results" :key="brand.tagId">
        <td>
          {{brand.name}}
          <div class="ArrayGroup">
            <div class="Item TagBg Border3" v-for="name in brand.names" :key="name">
              {{name}}
            </div>
          </div>
        </td>
        <td>
          <div class="ArrayGroup">
            <div class="Item TagBg Border3" v-for="tag in brand.tags" :key="tag.tagId">
              {{tag.name}}
            </div>
          </div>
        </td>
        <td>
          {{brand.price && brand.price.perPax}}
        </td>
        <td>
          <b-button class="Button" @click="$router.push({path: `/brands/${brand.brandId}`})" variant="outline-primary">
            Edit
          </b-button>
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
    }
  }
</script>

<style scoped lang="less">
  .Brands {
    margin-top: 15px;
  }

  .ArrayGroup {
    display: flex;
    flex-wrap: wrap;

    .Item {
      padding: 6px 10px;
      margin-right: 8px;
      margin-bottom: 8px;
    }
  }
</style>
