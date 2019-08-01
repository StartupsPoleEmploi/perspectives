<template>
    <div>
        <div class="temoignage px-5 font-size-md font-italic text-left" v-for="(temoignage, index) in temoignages"
             v-show="isTemoignageCourant(index + 1)">
            <img alt="Citation" width="35" height="35"
                 src="/assets/images/composants/temoignages/debut_citation.svg"
                 class="temoignage-citation temoignage-citation-debut"
                 v-bind:class="'temoignage-citation--' + theme" />
            <p class="texte-noir mb-0">{{temoignage.texte}}</p>
            <span class="texte-noir font-weight-bold">{{temoignage.source}}</span>
            <img alt="Citation" width="35" height="35"
                 src="/assets/images/composants/temoignages/fin_citation.svg"
                 class="temoignage-citation temoignage-citation-fin"
                 v-bind:class="'temoignage-citation--' + theme" />
        </div>
        <div class="paginationTemoignages">
            <span v-for="index in temoignages.length" v-on:click="chargerTemoignage(index)"
                  class="paginationTemoignages-item"
                  v-bind:class="[isTemoignageCourant(index) ? 'paginationTemoignages-pageCourante--' + theme : 'paginationTemoignages-page']">
            </span>
        </div>
    </div>
</template>

<script>
    export default {
        props: {
            temoignages: {
                type: Array,
                default: function () {
                    return [];
                }
            },
            theme: String
        },
        data: function () {
            return {
                indexTemoignage: 1
            }
        },
        mounted: function () {
            var self = this;
            if (this.temoignages.length > 0) {
                setInterval(function () {
                    self.carouselTemoignage();
                }, 3000);
            }
        },
        methods: {
            carouselTemoignage: function () {
                if (this.indexTemoignage === (this.temoignages.length)) {
                    this.chargerTemoignage(1);
                } else {
                    this.chargerTemoignage(this.indexTemoignage + 1);
                }
            },
            chargerTemoignage: function (index) {
                this.indexTemoignage = index;
            },
            isTemoignageCourant: function (index) {
                return this.indexTemoignage === index;
            }
        }
    }
</script>