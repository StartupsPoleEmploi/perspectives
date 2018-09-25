"use strict";

// FIXME : pas de jquery pour accéder à des éléments externes au composant
Vue.component('pagination', {
    props: ['criterePremierePage', 'criterePremierePageSuivante', 'paginationBaseUrl'],
    data: function () {
        return {
            pagesPrecedentes: this.criterePremierePage !== undefined ? [this.criterePremierePage] : [],
            criterePageSuivante: this.criterePremierePageSuivante,
            pageCourante: 0
        }
    },
    methods: {
        chargerPageSuivante: function() {
            var self = this;
            self.$http.get(self.paginationBaseUrl + '/' + encodeURIComponent(self.criterePageSuivante)).then(function(response) {
                $("#js-listeResultats").html(response.body);
                self.pagesPrecedentes.push(self.criterePageSuivante);
                self.criterePageSuivante = $("#js-dernierResultat").val();
                self.pageCourante = self.pagesPrecedentes.length - 1;
            }, function(response) {
                // erreur
            });
        },
        chargerPagePrecedente: function(index) {
            var self = this;
            self.$http.get(self.paginationBaseUrl + '/' + encodeURIComponent(self.pagesPrecedentes[index])).then(function(response) {
                $("#js-listeResultats").html(response.body);
                self.pageCourante = index;
            }, function(response) {
                // erreur
            });
        },
        isPageCourante: function(index) {
            return this.pageCourante === index;
        }
    },
    template:
    '<div class="pagination">' +
        '<a href="#" v-for="(page, index) in pagesPrecedentes" ' +
            'v-on:click="chargerPagePrecedente(index)" ' +
            'class="bouton pagination-item pagination-page" ' +
            'v-bind:class="[isPageCourante(index) ? \'pagination-pageCourante\' : \'bouton--noir\']">' +
            '{{index + 1}}' +
        '</a>' +
        '<a href="#" v-if="criterePageSuivante !== undefined && criterePageSuivante !== \'\'" ' +
            'v-on:click="chargerPageSuivante" class="bouton bouton--noir pagination-item pagination-page">></a>' +
    '</div>'
});