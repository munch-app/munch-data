<template>
  <b-container>

    <b-form @submit="onSubmit" @reset="onReset">

      <b-form-group label="Tag name:"
                    horizontal
                    label-for="exampleInput1">
        <b-form-input type="text"
                      v-model="data.name"
                      required
                      placeholder="Enter tag name eg. fast food">
        </b-form-input>
      </b-form-group>
      <b-form-group label="Alternative tag names:"
                    horizontal
                    label-for="exampleInput1">
        <b-form-input type="text"
                      v-model="data.names"
                      required
                      placeholder="Enter alternative tag names eg. western">
        </b-form-input>
      </b-form-group>

      <b-form-group label="Type of tag:"
                    horizontal
                    label-for="exampleInput1">
        <b-form-select v-model="data.type"
                       :options="options"
                       class="mb-3" />
      </b-form-group>

      <div class="Action">
        <b-button class="Button" type="submit" variant="primary">Submit</b-button>
        <b-button class="Button" type="reset" variant="danger">Reset</b-button>
      </div>


    </b-form>
  </b-container>
</template>

<script>

  export default {
    layout: 'manage',
    asyncData() {
      return {
        options: [
          'Food', 'Cuisine', 'Establishment', 'Amenities', 'Timing'
        ],
        data: {
          name: "",
          names: "",
          type: "",

        },
      }
    },
    methods: {
      onSubmit(evt) {
        evt.preventDefault();
        alert(JSON.stringify(this.data));
      },
      onReset(evt) {
        evt.preventDefault();
        this.data = {}
      }
    }
  }
</script>

<style scoped lang="less">


  .Action {
    margin-top: 36px;
    .Button {
      margin-right: 12px;
    }
  }
</style>
