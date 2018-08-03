<template>
  <b-container>
    <b-form @submit="onSubmit">
      <b-form-group label="Brand name:"
                    label-for="exampleInput1"
                    horizontal>
        <b-form-input type="text"
                      v-model="data.name"
                      required
                      placeholder="Enter brand name eg. Kentucky Fried Chicken">
        </b-form-input>
      </b-form-group>

      <b-form-group label="Alternative names:"
                    label-for="exampleInput2"
                    horizontal>
        <tags-edit :tags.sync="data.names" placeholder="Enter alternative names eg. KFC"></tags-edit>
      </b-form-group>
      <b-form-group label="Tags:"
                    label-for="exampleInput3"
                    horizontal>
        <tags-field v-if="tagsMapLoaded" :tags.sync="data.tags" :tagsMap="tagsMap"
                    placeholder="Enter tags eg. Fast Food, American"></tags-field>

      </b-form-group>
      <b-form-group label="Description:"
                    label-for="exampleInput9"
                    horizontal>
        <b-form-textarea rows="2"
                         max-rows="6"
                         v-model="data.description"
                         placeholder="Enter a short description eg. https://www.mcdonalds.com/ae/en-ae/full-menu.html">
        </b-form-textarea>
      </b-form-group>

      <br>
      <b-form-group label="Menu URL:"
                    label-for="exampleInput4"
                    horizontal>
        <b-form-input type="text"
                      v-model="data.menu.url"
                      placeholder="Enter menu URL eg. https://www.mcdonalds.com/ae/en-ae/full-menu.html">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Price:"
                    label-for="exampleInput5"
                    horizontal>
        <b-form-input type="text"
                      v-model="data.price.perPax"
                      placeholder="Enter price per pax eg. SGD 5">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Company name:"
                    label-for="exampleInput6"
                    horizontal>
        <b-form-input type="text"
                      v-model="data.company.name"
                      placeholder="Enter company name eg. Amazon">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Phone:"
                    label-for="exampleInput7"
                    horizontal>
        <b-form-input type="text"
                      v-model="data.phone"
                      placeholder="Enter contact no. eg. 12345678">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Website"
                    label-for="exampleInput8"
                    horizontal>
        <b-form-input type="text"
                      v-model="data.website"
                      placeholder="Enter website URL eg. https://www.mcdonalds.com/">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Country"
                    label-for="exampleInput9"
                    horizontal>
        <b-form-select v-model="data.location.country" :options="countries" class="mb-3"/>

      </b-form-group>
      <b-form-group label="Images:"
                    label-for="exampleInput10"
                    horizontal>
        <image-file-upload v-bind:images="data.images"></image-file-upload>
      </b-form-group>

      <div class="Action">
        <b-button class="Button" @click="onSubmit" variant="success">Submit</b-button>
        <b-button class="Button" variant="danger">Reset</b-button>
      </div>
    </b-form>
  </b-container>

</template>

<script>
  import moment from "moment";
  import ImageFileUpload from "../../components/ImageFileUpload";
  import TagsField from "../../components/TagsField";
  import TagsEdit from "../../components/TagsEdit";

  export default {
    components: {TagsEdit, TagsField, ImageFileUpload},
    layout: 'manage',

    asyncData({$axios, params}) {
      const brandId = params.brandId
      if (brandId !== '_') {
        return $axios.get(`/api/brands/${brandId}`)
          .then(({data}) => {
            return {data: data.data}
          })
      }
    },
    data() {
      return {
        countries: ["Singapore"],
        tagsMap: [],
        tagsMapLoaded: false,
        data: {
          name: "",
          names: [],
          tags: [],
          menu: {},
          price: {
            perPax: 10.0
          },
          location: {country: "Singapore"},
          company: {name: null},
          phone: "",
          website: "",
          description: "",
          images: [],
        }
      }
    },
    methods: {
      onSubmit(evt) {
        evt.preventDefault()
        this.complete(this.data);
      },

      onReset(evt) {
        evt.preventDefault();
        this.data = {}
      },

      complete(data) {
        const brandId = this.$route.params.brandId
        if (brandId === '_') {
          this.$axios.$post('/api/brands', data)
            .then((res) => {
              window.location.reload(true);
            });
        } else {
          this.$axios.$put('/api/brands/' + brandId, data).then((res) => {
            window.location.reload(true);
          });
        }
      }
    },

    async mounted() {
      const axios = this.$axios
      if (window.localStorage.getItem('tagsMap') == null || (moment().subtract(1, 'days')).isAfter(window.localStorage.getItem('tagsTimestamp'))) {
        //load in new tags
        loadTags([]).then(tags => {
          window.localStorage.setItem("tagsMap", JSON.stringify(tags))
          window.localStorage.setItem("tagsTimestamp", moment().format())
        })
      }
      const result = window.localStorage.getItem('tagsMap');
      this.tagsMap = JSON.parse(result);
      this.tagsMapLoaded = true;

      async function loadTags(tags, nextTagId) {
        return axios.$get('/api/tags?size=100&next.tagId=' + nextTagId || '')
          .then(function (res) {
            tags = tags.concat(res.data)

            if (res.next && res.next.tagId)
              return loadTags(tags, res.next && res.next.tagId)
            else
              return Promise.resolve(tags)
          })
      }
    },
  }
</script>

<style scoped lang="less">
  .Action {
    margin-top: 36px;
    margin-bottom: 64px;
    float: right;
    .Button {
      margin-right: 12px;
    }
  }

  .TagInput {
    font-family: inherit;
    font-size: 16px;
    width: 100%;
    padding: 6px 12px;
    box-sizing: border-box;
    border: 1px solid #ced4da;
    border-radius: 0.25rem;
  }
</style>
