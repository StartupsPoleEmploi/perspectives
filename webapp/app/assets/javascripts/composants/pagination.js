"use strict";

var pagination = Vue.component('pagination', {
    props: {
        pagesInitiales: Array,
        nbParPage: Number
    },
    data: function () {
        return {
            pagesPrecedentes: (this.pagesInitiales !== undefined && this.pagesInitiales.length > 0) ? this.pagesInitiales.slice(0, this.pagesInitiales.length - 1) : [],
            criterePageSuivante: (this.pagesInitiales !== undefined && this.pagesInitiales.length > 1) ? this.pagesInitiales[this.pagesInitiales.length - 1] : undefined,
            pageCourante: 0
        }
    },
    methods: {
        chargerPageSuivante: function() {
            this.$emit("charger-page-suivante", this.criterePageSuivante);
        },
        chargerPagePrecedente: function(index) {
            this.$emit("charger-page-precedente", this.pagesPrecedentes[index], index);
        },
        pageSuivanteChargee: function(nbResultat, pageSuivante) {
            this.pagesPrecedentes.push(this.criterePageSuivante);
            this.pageCourante = this.pagesPrecedentes.length - 1;

            if (nbResultat === this.nbParPage) {
                this.criterePageSuivante = pageSuivante;
            } else {
                this.criterePageSuivante = undefined; // dernière page atteinte
            }
        },
        pagePrecedenteChargee: function(index) {
            this.pageCourante = index;
        },
        isPageCourante: function(index) {
            return this.pageCourante === index;
        },
        modifierPagination: function(pages) {
            this.pagesPrecedentes = (pages !== undefined && pages.length > 0) ? pages.slice(0, pages.length - 1) : [];
            this.criterePageSuivante = (pages !== undefined && pages.length > 1) ? pages[pages.length - 1] : undefined;
            this.pageCourante = 0;
        }
    },
    template:
    '<div class="pagination">' +
        '<a href="#" v-for="(page, index) in pagesPrecedentes" ' +
            'v-on:click="chargerPagePrecedente(index)" ' +
            'class="bouton pagination-item" ' +
            'v-bind:class="[isPageCourante(index) ? \'pagination-pageCourante\' : \'pagination-page\']">' +
            '{{index + 1}}' +
        '</a>' +
        '<a href="#" v-if="criterePageSuivante !== undefined" ' +
            'v-on:click="chargerPageSuivante" class="bouton pagination-item pagination-page">></a>' +
    '</div>'
});

export default pagination;