<template>
  <div>
    <dropzone id="foo" ref="el" :options="options" :destroyDropzone="true"
              @vdropzone-success="uploadedImage"
              @vdropzone-removed-file="removedImage"
    ></dropzone>
  </div>
</template>

<script>
  import Dropzone from 'nuxt-dropzone'
  import 'nuxt-dropzone/dropzone.css'

  export default {
    name: "ImageFileUpload",
    props: ['images'],
    components: {
      Dropzone
    },
    data() {
      return {
        // See https://rowanwins.github.io/vue-dropzone/docs/dist/index.html#/props
        options: {
          url: "/api/files/images/upload",
          acceptedFiles: "image/*",
          addRemoveLinks: true,
        },
        mutatedImages: JSON.parse(JSON.stringify(this.images))
      }
    },
    mounted() {
      // Everything is mounted and you can access the dropzone instance
      const instance = this.$refs.el.dropzone

      this.images.forEach(function (image) {
        let smallest = image.sizes.reduce(function(prev, curr) {
          return prev.width < curr.width ? prev : curr;
        });
        let mockFile = {imageId: image.imageId, name: image.imageId, size: 0}
        instance.emit("addedfile", mockFile);
        instance.emit("thumbnail", mockFile, smallest.url);
        instance.createThumbnailFromUrl(mockFile, smallest.url);
        instance.emit("complete", mockFile);
      })
    },
    methods: {
      uploadedImage(file, body) {
        file.iamgeId = body.data.imageId
        this.images.push(body.data)
      },
      removedImage(file) {
        const index = this.images.findIndex(function (image) {
          return image.imageId === file.imageId
        })
        this.$delete(this.images, index)
      }
    }
  }
</script>

<style scoped lang="less">

</style>
