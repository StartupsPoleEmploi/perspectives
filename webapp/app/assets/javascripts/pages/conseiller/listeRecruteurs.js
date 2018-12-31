"use strict";

var app = new Vue({
    el: '#listeRecruteurs',
    data: function() {
        return {
            csrfToken: jsData.csrfToken,
            recruteurs: jsData.recruteurs,
            pagesInitiales: jsData.pagesInitiales,
            nbRecruteursParPage: jsData.nbRecruteursParPage
        }
    },
    methods: {
        chargerPageSuivante: function(critere) {
            this.paginerRecruteurs(critere).done(function (response) {
                app.recruteurs = response.recruteurs;
                app.$refs.pagination.pageSuivanteChargee(response.recruteurs.length, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function(critere, index) {
            this.paginerRecruteurs(critere).done(function (response) {
                app.recruteurs = response.recruteurs;
                app.$refs.pagination.pagePrecedenteChargee(index);
            });
        },
        paginerRecruteurs: function (critere) {
            return $.ajax({
                type: 'POST',
                url: '/conseiller/paginerRecruteurs',
                data: [
                    {name: "csrfToken", value: this.csrfToken},
                    {name: "dateInscription", value: critere.dateInscription},
                    {name: "recruteurId", value: critere.recruteurId}
                ],
                dataType: 'json'
            });
        }
    }
});