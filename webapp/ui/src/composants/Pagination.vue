<template>
    <div class="pagination text-center" v-if="nbPages > 1">
        <a href="#" class="bouton pagination-item"
           v-for="index in nbPages"
           v-on:click="chargerPage(index)"
           v-bind:class="[index === indexPageCourante ? 'pagination-pageCourante' : 'pagination-page']">
            {{index}}
        </a>
        <a href="#" class="bouton pagination-item pagination-page" title="Page suivante"
           v-if="indexPageCourante !== nbPages"
           v-on:click="chargerPage(indexPageCourante + 1)">
           >
        </a>
    </div>
</template>

<script>
    export default {
        props: {
            nbPages: Number
        },
        data: function () {
            return {
                indexPageCourante: 1
            }
        },
        methods: {
            chargerPage: function (index) {
                this.$emit("charger-page", index);
            },
            pageChargee: function(index) {
              this.indexPageCourante = index;
            },
            getPageCourante: function () {
                return this.indexPageCourante;
            }
        }
    }
</script>