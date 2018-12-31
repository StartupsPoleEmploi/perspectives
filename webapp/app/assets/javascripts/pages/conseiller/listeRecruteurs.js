"use strict";

var app = new Vue({
    el: '#listeRecruteurs',
    data: function() {
        return {
            csrfToken: jsData.csrfToken,
            pagesInitiales: jsData.pagesInitiales,
            nbRecruteursParPage: jsData.nbRecruteursParPage
        }
    },
    methods: {
        chargerPageSuivante: function(critere) {
            this.paginerRecruteurs(critere).done(function (response) {
                app.$refs.recruteurs.innerHTML = response.html;
                app.$refs.pagination.pageSuivanteChargee(response.nbRecruteurs, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function(critere, index) {
            this.paginerRecruteurs(critere).done(function (response) {
                app.$refs.recruteurs.innerHTML = response.html;
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