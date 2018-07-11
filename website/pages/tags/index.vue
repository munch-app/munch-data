<template>
  <b-container class="Container">
    <div class="Header">
      <h2 class="Title">Munch Tag</h2>
      <b-button class="Create" variant="outline-primary" href="tags/_">Create</b-button>
    </div>

    <b-row class="Search">
      <b-col>
        <b-form-input class="Field" type="text" placeholder="Querying name or names"></b-form-input>
      </b-col>
      <b-col>
        <b-button class="Button" variant="primary">Search</b-button>
      </b-col>
    </b-row>

    <div class="Results">
      <h4>Search Results</h4>
      <b-row v-for="tag in results" :key="tag.tagId">
        <b-col>
          <h5>{{tag.name}}</h5>
        </b-col>
        <b-col>

        </b-col>
      </b-row>
    </div>
  </b-container>
</template>

<script>
  export default {
    layout: 'manage',
    data() {
      return {
        results: [
          {
            tagId: "123",
            name: "Chicken Wing",
            type: "Food"
          },
          {
            tagId: "123",
            name: "Chinese",
            type: "Cuisine"
          }
        ]
      }
    }
  }
</script>

<style scoped lang="less">
  .Container {
    margin-top: 24px;
    margin-bottom: 24px;
  }

  .Block {
    padding-top: 12px;
    padding-bottom: 12px;
  }

  .Header:extend(.Block) {
    .Title {
      float: left;
    }
    .Create {
      float: right;
    }
  }

  .Search:extend(.Block) {
    clear: both;

    .Field {
      font-size: 1.25rem;
    }

    .Button {
      font-size: 1.25rem;
    }
  }

  .Results:extend(.Block) {
    clear: both;
  }
</style>
