import Vue from 'vue';

var component = Vue.component('temoignages', {
    props: {
        temoignages: Array
    },
    data: function () {
        return {
            indexTemoignageCourant: (this.temoignages !== undefined && this.temoignages.length > 0) ? 0 : null
        }
    },
    mounted: function () {
        var self = this;
        setInterval(function () {
            self.carouselTemoignage();
        }, 3000);
    },
    methods: {
        carouselTemoignage: function() {
            if (this.indexTemoignageCourant === (this.temoignages.length - 1)) {
                this.chargerTemoignage(0);
            } else {
                this.chargerTemoignage(this.indexTemoignageCourant + 1);
            }
        },
        chargerTemoignage: function(index) {
            this.temoignageCourant = this.temoignages[index];
            this.indexTemoignageCourant = index;
        },
        isTemoignageCourant: function(index) {
            return this.indexTemoignageCourant === index;
        }
    },
    template:
        '<div> ' +
            '<div class="temoignage" v-for="(temoignage, index) in temoignages" v-show="isTemoignageCourant(index)"> ' +
                '<img alt="Citation" class="temoignage-image temoignage-debutCitation" width="35" height="35" src="/assets/images/composants/temoignages/debut_citation.svg" /> ' +
                '<p class="texte-noir mb-0">{{temoignage.texte}}</p> ' +
                '<span class="texte-noir font-weight-bold">{{temoignage.source}}</span> ' +
                '<img alt="Citation" class="temoignage-image temoignage-finCitation" width="35" height="35" src="/assets/images/composants/temoignages/fin_citation.svg" /> ' +
            '</div> ' +
            '<div class="paginationTemoignages"> ' +
                '<span v-for="(temoignage, index) in temoignages" v-on:click="chargerTemoignage(index)"' +
                      'class="paginationTemoignages-item" ' +
                      'v-bind:class="[isTemoignageCourant(index) ? \'paginationTemoignages-pageCourante\' : \'paginationTemoignages-page\']"></span> ' +
            '</div> ' +
        '</div>'
});

export default component;