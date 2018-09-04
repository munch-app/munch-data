<template>
  <b-container class="Container">
    <div class="Header">
      <h2 class="Title">Munch Brand</h2>
      <b-button class="Create" variant="outline-primary" href="brands/_">Create</b-button>
    </div>

    <b-row class="Search">
      <b-col>
        <b-form-input
          class="Field"
          type="text"
          v-model="searchValue"
          placeholder="Querying name or names"
        ></b-form-input>
      </b-col>
      <b-col>
        <b-button class="Button" @click="search" variant="primary">Search Name</b-button>
      </b-col>
    </b-row>

    <div class="Results">
      <h4>Search Results</h4>
      <brands-list :results="results"></brands-list>
    </div>
  </b-container>
</template>

<script>
  import BrandsList from "../../components/BrandsList";

  const defaultQuery = {
    from: 0, size: 25,
    query: {
      bool: {
        "filter": [
          {"term": {"dataType": "Brand"}},
        ]
      }
    }
  }

  export default {
    components: {BrandsList},
    layout: 'manage',
    data() {
      return {
        searchValue: ""
      }
    },
    asyncData(context) {
      return context.$axios.$post('/api/search/brands', defaultQuery)
        .then(({data}) => {
          return {
            results: data,
          }
        });
    },
    mounted() {
      window.addEventListener('keyup', this.keyUpListener);
    },
    methods: {
      keyUpListener(evt) {
        switch (evt.keyCode) {
          // Enter
          case 13:
            this.search();
            break;
        }
      },

      search() {
        if (this.searchValue.match(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/)) {
          this.$router.get({path: '/brands/' + this.searchValue})
        } else if (this.searchValue !== '') {
          this.onSearchName()
        } else {
          this.onDefaultSearch()
        }
      },

      onSearchName() {
        this.$axios.$post('/api/search/brands', {
          from: 0, size: 25,
          query: {
            bool: {
              "filter": [
                {
                  "term": {
                    "dataType": "Brand"
                  }
                }
              ],
              "must": [
                {
                  "multi_match": {
                    "type": "phrase_prefix",
                    "query": this.searchValue,
                    "fields": ["name", "names"]
                  }
                },
              ]
            }
          }
        }).then(({data}) => {
          this.results = data
        });
      },

      onDefaultSearch() {
        this.$axios.$post('/api/search/brands', defaultQuery)
          .then(({data}) => {
            this.results = data
          });
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
