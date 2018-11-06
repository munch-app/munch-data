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
                         placeholder="Enter a short description">
        </b-form-textarea>
      </b-form-group>

      <br>
      <b-form-group label="Menu URL:"
                    label-for="exampleInput4"
                    horizontal>
        <b-form-input type="text"
                      :value="menuUrl" @input="data.menu.url = $event"
                      placeholder="Enter menu URL">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Price:"
                    label-for="exampleInput5"
                    horizontal>
        <b-form-input type="number"
                      :value="pricePerPax" @input="data.price.perPax = $event"
                      placeholder="Enter price per pax">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Company name:"
                    label-for="exampleInput6"
                    horizontal>
        <b-form-input type="text" :value="companyName" @input="data.company.name = $event"
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
        <b-form-input type="text" v-model="data.website"
                      placeholder="Enter website URL eg. https://www.mcdonalds.com/">
        </b-form-input>
      </b-form-group>

      <b-form-group label="Country & City:"
                    label-for="location"
                    horizontal>
        <b-form-select v-model="data.location.country" :options="countries" class="mb-3"/>
      </b-form-group>

      <b-form-group label="Settings:"
                    label-for="exampleInput9"
                    horizontal>
        <b-row>
          <b-col>
            Brand Status
            <b-form-select v-model="data.status.type" :options="['open', 'closed']" class="mb-3"/>
          </b-col>
          <b-col>
            Catalyst Brand Plugin Auto Link?
            <b-form-select v-model="data.place.autoLink" :options="[true, false]" class="mb-3"/>
          </b-col>
        </b-row>
      </b-form-group>

      <b-form-group label="Images:"
                    label-for="images"
                    horizontal>
        <image-file-upload v-bind:images="data.images"></image-file-upload>
      </b-form-group>

      <div class="Action">
        <b-button class="Button" v-b-modal.deleteModal v-if="data.brandId" variant="danger">Delete</b-button>
        <b-button class="Button" @click="complete(data)" variant="success">Submit</b-button>
      </div>

      <b-modal id="deleteModal" hide-footer title="Dangerous Action">
        <p class="my-4">Delete {{data.name}} permanently.</p>
        <b-button class="Button" @click="onDelete" variant="danger">Delete</b-button>
      </b-modal>
    </b-form>
  </b-container>

</template>

<script>
  import moment from "moment";
  import ImageFileUpload from "../../components/ImageFileUpload";
  import TagsField from "../../components/TagsField";
  import TagsEdit from "../../components/TagsEdit";

  String.prototype.isBlank = function () {
    return !(this && this.trim())
  }

  String.prototype.isNotBlank = function () {
    return this && this.trim()
  }

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
        countries: ["SGP"],
        cities: ["singapore"],

        tagsMap: [],
        tagsMapLoaded: false,
        data: {
          name: "",
          phone: "",
          website: "",
          description: "",

          names: [],
          tags: [],
          images: [],

          place: {autoLink: true},
          status: {type: 'open'},

          location: {country: "SGP"},

          menu: {},
          price: {},
          company: {}
        }
      }
    },
    computed: {
      menuUrl() {
        return this.data.menu && this.data.menu.url
      },
      pricePerPax() {
        return this.data.price && this.data.price.perPax
      },
      companyName() {
        return this.data.company && this.data.company.name
      }
    },
    methods: {
      onSubmit(evt) {
        evt.preventDefault()
      },

      onDelete() {
        this.$axios.$delete(`/api/brands/${this.data.brandId}`).then((res) => {
          this.$router.push({path: '/brands'})
        });
      },

      validate(data) {
        if (!data.name && data.name.isBlank()) delete data.name
        if (!data.phone && data.phone.isBlank()) delete data.phone
        if (!data.website && data.website.isBlank()) delete data.website
        if (!data.description && data.description.isBlank()) delete data.description

        if (data.menu && (!data.menu.url || data.menu.url.isBlank())) {
          delete data.menu
        }

        if (data.price && !data.price.perPax) {
          delete data.price
        }

        if (data.company && (!data.company.name || data.company.name.isBlank())) {
          delete data.company
        }

        return data
      },

      complete(data) {
        data = this.validate(data)
        console.log(data)

        const brandId = this.$route.params.brandId
        if (brandId === '_') {
          this.$axios.$post('/api/brands', data)
            .then((res) => {
              this.$router.push({path: '/brands'})
            })
            .catch((error) => {
              alert(error)
            })
        } else {
          this.$axios.$put('/api/brands/' + brandId, data).then((res) => {
            this.$router.push({path: '/brands'})
          }).catch((error) => {
            alert(error)
          })
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
