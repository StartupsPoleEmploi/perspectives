import Vue from 'vue';
import $ from 'jquery';
import Pagination from '../../composants/Pagination.vue';
import '../../commun/filters.js';

var app = new Vue({
    el: '#listeRecruteurs',
    components: {
        'pagination': Pagination
    },
    data: function() {
        return {
            csrfToken: jsData.csrfToken,
            nbRecruteursParPage: jsData.nbRecruteursParPage,
            recruteurs: jsData.recruteurs,
            pages: jsData.pages
        }
    },
    methods: {
        chargerPage: function(index) {
            var filtrePage = this.pages[index - 1];

            return $.ajax({
                type: 'POST',
                url: '/conseiller/paginerRecruteurs',
                data: [
                    {name: "csrfToken", value: this.csrfToken},
                    {name: "dateInscription", value: filtrePage.dateInscription},
                    {name: "recruteurId", value: filtrePage.recruteurId}
                ],
                dataType: 'json'
            }).done(function (response) {
                app.recruteurs = response.recruteurs;

                if (index === app.pages.length) {
                    if (response.recruteurs.length === app.nbRecruteursParPage) {
                        app.pages.push(response.pageSuivante);
                    }
                }
                app.$refs.pagination.pageChargee(index);
            });
        }
    }
});