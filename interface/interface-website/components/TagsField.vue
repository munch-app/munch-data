<template>
  <div>
    <div
      :class="{
        'read-only': readOnly,
        'vue-input-tag-wrapper--active': isInputActive,
      }"
      class="vue-input-tag-wrapper">
      <span v-for="(tag, index) in innerTags" :key="index" class="input-tag">
        <span>{{ tag.name }}</span>
        <a v-if="!readOnly" @click.prevent.stop="remove(index)" class="remove"></a>
      </span>
      <input
        v-if="!readOnly && !isLimit"
        :placeholder="placeholder"
        type="text"
        v-model="newTag"
        v-on:keydown.delete.stop="removeLastTag"
        class="new-tag"
        @input="onChange"
        @keydown.down="onArrowDown"
        @keydown.up="onArrowUp"
        @keydown.enter="onEnter"
      />
    </div>
    <div class="autocomplete">
      <ul class="autocomplete-results" v-show="isOpen">
        <li class="autocomplete-result"
            v-for="(result, i) in results"
            :key="i"
            v-if="innerTags.indexOf(result)===-1"
            :class="{ 'is-active': i === arrowCounter }"
            @click="setResult(result)">
          {{result.name}}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'TagsField',
    props: {
      tags: {
        type: Array,
        default: () => []
      },
      tagsMap: {
        type: Array,
        default: () => []
      },
      placeholder: {
        type: String,
        default: ''
      },
      readOnly: {
        type: Boolean,
        default: false
      },
      limit: {
        default: -1
      }
    },
    data() {
      return {
        newTag: '',
        innerTags: [...this.tags],
        isInputActive: false,
        results: this.tagsMap,
        isOpen: false,
        arrowCounter: 0,
        tagCollection: this.tagsMap
      }
    },
    watch: {
      tags() {
        this.innerTags = [...this.tags]
      }
    },
    computed: {
      isLimit: function () {
        return this.limit > 0 && Number(this.limit) === this.innerTags.length
      }
    },
    methods: {
      addNew() {
        this.innerTags.push(this.newTag)
        this.newTag = ''
        this.tagChange()
      },
      remove(index) {
        this.innerTags.splice(index, 1)
        this.tagChange()
      },
      removeLastTag() {
        if (this.newTag) {
          return
        }
        this.innerTags.pop()
        this.tagChange()
      },
      tagChange() {
        this.$emit('update:tags', this.innerTags)
      },
      onChange() {
        this.isOpen = true;
        this.filterResults();
      },
      onArrowDown() {
        if (this.arrowCounter < this.results.length) {
          this.arrowCounter = this.arrowCounter + 1;
        }
      },
      onArrowUp() {
        if (this.arrowCounter > 0) {
          this.arrowCounter = this.arrowCounter - 1;
        }
      },
      onEnter() {
        this.setResult(this.results[this.arrowCounter]);
      },
      handleClickOutside(evt) {
        if (!this.$el.contains(evt.target)) {
          this.isOpen = false;
          this.arrowCounter = 0;
        }
      },
      filterResults() {
         this.results =
           this.tagCollection.filter(item => item.name.toLowerCase().indexOf(this.newTag.toLowerCase()) > -1)
      },
      setResult(result) {
        this.newTag = result;
        this.addNew()
        this.isOpen = false;
        this.arrowCounter = 0;
      },
    },
    mounted() {
      document.addEventListener('click', this.handleClickOutside);
    },
    destroyed() {
      document.removeEventListener('click', this.handleClickOutside);
    }
  }
</script>

<style>
  .autocomplete {
    position: relative;
  }

  .autocomplete-result.is-active,
  .autocomplete-result:hover {
    background-color: #fdc035;
    color: white;
  }

  .autocomplete-results {
    padding: 2px;
    margin: 0;
    border: 1px solid #eeeeee;
    height: 120px;
    overflow: auto;
  }

  .autocomplete-result {
    list-style: none;
    text-align: left;
    padding: 1px 8px;
    cursor: pointer;
  }

  .vue-input-tag-wrapper {
    background-color: #fff;
    border: 1px solid #a5d24a;
    border-radius: 2px;
    overflow: hidden;
    padding-left: 4px;
    padding-top: 4px;
    cursor: text;
    text-align: left;
    -webkit-appearance: textfield;
    display: flex;
    flex-wrap: wrap;
  }

  .vue-input-tag-wrapper .input-tag {
    background-color: #fdc035;
    border: 1px solid #fdbf2f;
    border-radius: 2px;
    color: #000;
    display: inline-block;
    font-size: 16px;
    font-weight: 400;
    margin-bottom: 4px;
    margin-right: 4px;
    padding: 2px 5px 2px 5px;
  }

  .vue-input-tag-wrapper .input-tag .remove {
    cursor: pointer;
    font-weight: bold;
    color: #faf4ff;
  }

  .vue-input-tag-wrapper .input-tag .remove:hover {
    text-decoration: none;
  }

  .vue-input-tag-wrapper .input-tag .remove::before {
    content: " x";
  }

  .vue-input-tag-wrapper .new-tag {
    background: transparent;
    border: 0;
    color: #777;
    font-size: 16px;
    font-weight: 400;
    margin-bottom: 6px;
    margin-top: 1px;
    outline: none;
    padding: 5px 10px;
    flex-grow: 1;
  }

  .vue-input-tag-wrapper.read-only {
    cursor: default;
  }
</style>
