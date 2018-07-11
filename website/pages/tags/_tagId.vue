<template>
  <b-container>
    <b-form @submit="onSubmit" @reset="onReset">
      <b-form-group label="Tag name:"
                    label-for="exampleInput1">
        <b-form-input type="text"
                      v-model="data.name"
                      required
                      placeholder="Enter name">
        </b-form-input>
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
        data: {
          name: "Chinese"
        }
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
