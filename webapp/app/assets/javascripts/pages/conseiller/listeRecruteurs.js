"use strict";

// FIXME : pas de jquery pour accéder à des éléments externes au composant
var app = new Vue({
    el: '#listeRecruteurs',
    methods: {
        chargerPageSuivante: function(critere) {
             this.$http.get('/conseiller/recruteurs/' + encodeURIComponent(critere)).then(function(response) {
                app.$refs.recruteurs.innerHTML = response.body;
                app.$refs.pagination.pageSuivanteChargee(Number($("#js-nbResultats").val()), $("#js-dernierResultat").val());
            }, function(response) {
                // erreur
            });
        },
        chargerPagePrecedente: function(critere, index) {
            this.$http.get('/conseiller/recruteurs/' + encodeURIComponent(critere)).then(function(response) {
                app.$refs.recruteurs.innerHTML = response.body;
                app.$refs.pagination.pagePrecedenteChargee(index);
            }, function(response) {
                // erreur
            });
        }
    }
});