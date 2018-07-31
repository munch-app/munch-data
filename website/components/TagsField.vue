<template>
  <div>
    <div
      @click="focusNewTag()"
      :class="{
        'read-only': readOnly,
        'vue-input-tag-wrapper--active': isInputActive,
      }"
      class="vue-input-tag-wrapper">
      <span v-for="(tag, index) in innerTags" :key="index" class="input-tag">
        <span>{{ tag }}</span>
        <a v-if="!readOnly" @click.prevent.stop="remove(index)" class="remove"></a>
      </span>
      <input
        v-if                     = "!readOnly && !isLimit"
        ref                      = "inputtag"
        :placeholder             = "placeholder"
        type                     = "text"
        v-model                  = "newTag"
        v-on:keydown.delete.stop = "removeLastTag"
        v-on:keydown             = "addNew"
        v-on:blur                = "handleInputBlur"
        v-on:focus               = "handleInputFocus"
        class                    = "new-tag"
        @input                   = "onChange"
      />
    </div>
    <div class="autocomplete">
      <ul class="autocomplete-results" v-show="isOpen">
        <li class="autocomplete-result"
            v-for="(result, i) in results"
            :key="i"
            @click="setResult(result)">
          {{result}}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
  /* eslint-disable */
  const validators = {
    email: new RegExp(/^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/),
    url: new RegExp(/^(https?|ftp|rmtp|mms):\/\/(([A-Z0-9][A-Z0-9_-]*)(\.[A-Z0-9][A-Z0-9_-]*)+)(:(\d+))?\/?/i),
    text: new RegExp(/^[a-zA-Z]+$/),
    digits: new RegExp(/^[\d() \.\:\-\+#]+$/),
    isodate: new RegExp(/^\d{4}[\/\-](0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])$/)
  }

  /* eslint-enable */
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
      validate: {
        type: String | Object,
        default: ''
      },
      addTagOnKeys: {
        type: Array,
        default: function () {
          return [
            13, // Return
            188, // Comma ','
            // 9 // Tab
          ]
        }
      },
      addTagOnBlur: {
        type: Boolean,
        default: false
      },
      limit: {
        default: -1
      }
    },
    data () {
      return {
        newTag: '',
        innerTags: [...this.tags],
        isInputActive: false,
        results: this.tagsMap,
        isOpen: false,
      }
    },
    watch: {
      tags () {
        this.innerTags = [...this.tags]
      }
    },
    computed: {
      isLimit: function () {
        return this.limit > 0 && Number(this.limit) === this.innerTags.length
      }
    },
    methods: {
      focusNewTag () {
        if (this.readOnly || !this.$el.querySelector('.new-tag')) { return }
        this.$el.querySelector('.new-tag').focus()
      },
      handleInputFocus () {
        this.isInputActive = true
      },
      handleInputBlur (e) {
        this.isInputActive = false
        this.addNew(e)
      },
      addNew (e) {
        // Do nothing if the current key code is
        // not within those defined within the addTagOnKeys prop array.
        if ((e && this.addTagOnKeys.indexOf(e.keyCode) === -1 &&
          (e.type !== 'blur' || !this.addTagOnBlur)) || this.isLimit) {
          return
        }
        if (e) {
          e.stopPropagation()
          e.preventDefault()
        }
        if (
          this.newTag &&
          this.innerTags.indexOf(this.newTag) === -1 &&
          this.validateIfNeeded(this.newTag)
        ) {
          this.innerTags.push(this.newTag)
          this.newTag = ''
          this.tagChange()
        }
      },
      validateIfNeeded (tagValue) {
        if (this.validate === '' || this.validate === undefined) {
          return true
        } else if (typeof (this.validate) === 'string' && Object.keys(validators).indexOf(this.validate) > -1) {
          return validators[this.validate].test(tagValue)
        } else if (typeof (this.validate) === 'object' && this.validate.test !== undefined) {
          return this.validate.test(tagValue)
        }
        return true
      },
      remove (index) {
        this.innerTags.splice(index, 1)
        this.tagChange()
      },
      removeLastTag () {
        if (this.newTag) { return }
        this.innerTags.pop()
        this.tagChange()
      },
      tagChange () {
        this.$emit('update:tags', this.innerTags)
      },
      onChange() {
        this.isOpen = true;
        //this.filterResults();
      },
      filterResults() {
        console.log(this.results.map(a=>a.names[0].toLowerCase()));
        this.results = this.results.map(a=>a.names[0].toLowerCase().filter(item => item.indexOf(this.search.toLowerCase()) > -1));
      },
      setResult(result) {
        this.newTag = result;
        this.addNew(event.keyCode == 13)
        this.isOpen = false;
      },
    }
  }
</script>

<style>
  .autocomplete {
    position: relative;
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

  .autocomplete-result:hover {
    background-color: #fdc035;
    color: white;
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
