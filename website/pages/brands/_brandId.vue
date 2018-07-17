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
        <b-form-input type="text"
                      v-model="data.names"
                      placeholder="Enter alternative names eg. KFC">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Tags:"
                    label-for="exampleInput3"
                    horizontal>
        <b-form-input type="text"
                      v-model="data.tags"
                      required
                      placeholder="Enter tags eg. fast food, restaurant">
        </b-form-input>
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
      <b-form-group label="Images:"
                    label-for="exampleInput10"
                    horizontal>
        <image-file-upload v-bind:images="data.images"></image-file-upload>
      </b-form-group>


      <div class="Action">
        <b-button class="Button" type="submit" variant="primary">Submit</b-button>
        <b-button class="Button" type="reset" variant="danger">Reset</b-button>
      </div>
    </b-form>
  </b-container>

</template>

<script>

  import ImageFileUpload from "../../components/ImageFileUpload";

  export default {
    components: {ImageFileUpload},
    layout: 'manage',
    asyncData() {
      return {
        data: {
          name: "",
          names: [""],
          tags: [""],
          menu: {},
          price: {
            perPax: 10.0
          },
          company: {name: null},
          phone: "",
          website: "",
          description: "",

          images: [
            {
              imageId: "123",
              profile: {
                type: "munch-data",
                id: "123",
                name: "Person"
              },
              sizes: [
                {
                  width: 160,
                  height: 180,
                  url: "http://via.placeholder.com/160x180"
                },
                {
                  width: 320,
                  height: 360,
                  url: "http://via.placeholder.com/320x360"
                }
              ]
            }
          ],
        }
      }
    },

    methods: {
      onSubmit(evt) {
        evt.preventDefault();
        complete(this.data);
        // alert(JSON.stringify(this.data));
      },

      onReset(evt) {
        evt.preventDefault();
        this.data = {}
      },

      complete(data) {
        const brandId = this.$route.params.brandId
        if (brandId === '_') {
          this.$axios.$post('/api/brands', data).then((res) => {
            window.location.reload(true);
          });
        } else {
          this.$axios.$put('/api/brands/'+brandId, data).then((res) => {
            window.location.reload(true);
          });
        }

      }
    }
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
</style>
