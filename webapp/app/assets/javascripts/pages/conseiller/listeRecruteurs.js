"use strict";

var app = new Vue({
    el: '#listeRecruteurs',
    data: function() {
        return {
            pagesInitiales: jsData.pagesInitiales,
            nbRecruteursParPage: jsData.nbRecruteursParPage
        }
    },
    methods: {
        chargerPageSuivante: function(critere) {
            $.ajax({
                type: 'GET',
                url: '/conseiller/paginationRecruteurs' +
                '?dateInscription=' + encodeURIComponent(critere.dateInscription) +
                '&recruteurId=' + encodeURIComponent(critere.recruteurId),
                dataType: 'json'
            }).done(function (response) {
                app.$refs.recruteurs.innerHTML = response.html;
                app.$refs.pagination.pageSuivanteChargee(response.nbRecruteurs, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function(critere, index) {
            $.ajax({
                type: 'GET',
                url: '/conseiller/paginationRecruteurs' +
                '?dateInscription=' + encodeURIComponent(critere.dateInscription) +
                '&recruteurId=' + encodeURIComponent(critere.recruteurId),
                dataType: 'json'
            }).done(function (response) {
                app.$refs.recruteurs.innerHTML = response.html;
                app.$refs.pagination.pagePrecedenteChargee(index);
            });
        }
    }
});