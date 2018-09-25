"use strict";

// FIXME : pas de jquery pour accéder à des éléments externes au composant
Vue.component('pagination', {
    props: ['criterePremierePage', 'criterePremierePageSuivante', 'paginationBaseUrl'],
    data: function () {
        return {
            pagesPrecedentes: this.criterePremierePage !== undefined ? [this.criterePremierePage] : [],
            criterePageSuivante: this.criterePremierePageSuivante
        }
    },
    methods: {
        chargerPageSuivante: function() {
            var self = this;
            self.$http.get(self.paginationBaseUrl + '/' + encodeURIComponent(self.criterePageSuivante)).then(function(response) {
                $("#js-listeResultats").html(response.body);
                self.pagesPrecedentes.push(self.criterePageSuivante);
                self.criterePageSuivante = $("#js-dernierResultat").val();
            }, function(response) {
                // erreur
            });
        },
        chargerPagePrecedente: function(index) {
            var self = this;
            self.$http.get(self.paginationBaseUrl + '/' + encodeURIComponent(self.pagesPrecedentes[index])).then(function(response) {
                $("#js-listeResultats").html(response.body);
            }, function(response) {
                // erreur
            });
        }
    },
    template:
    '<div>' +
        '<span>' +
            '<button v-for="(page, index) in pagesPrecedentes" ' +
                    'v-on:click="chargerPagePrecedente(index)" class="bouton" type="button">' +
                '{{index + 1}}' +
            '</button>' +
        '</span>' +
        '<button v-if="criterePageSuivante !== undefined && criterePageSuivante !== \'\'" ' +
                'v-on:click="chargerPageSuivante" class="bouton" type="button">Page suivante</button>' +
    '</div>'
});