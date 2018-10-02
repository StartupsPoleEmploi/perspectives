"use strict";

$(document).ready(function () {
    $("body").on("click", ".js-declarerRepriseEmploi", function (e) {
        e.preventDefault();
        var bouton = $(this);
        $.ajax({
            type: "GET",
            url: bouton.attr("href"),
            dataType: "text"
        }).done(function () {
            bouton.hide();
            bouton.parent().html("Non");
        });
    });
});

// FIXME : pas de jquery pour accéder à des éléments externes au composant
var app = new Vue({
    el: '#listeCandidats',
    methods: {
        chargerPageSuivante: function(critere) {
            this.$http.get('/conseiller/candidats/' + encodeURIComponent(critere)).then(function(response) {
                app.$refs.candidats.innerHTML = response.body;
                app.$refs.pagination.pageSuivanteChargee($("#js-nbResultats").val(), $("#js-dernierResultat").val());
            }, function(response) {
                // erreur
            });
        },
        chargerPagePrecedente: function(critere, index) {
            this.$http.get('/conseiller/candidats/' + encodeURIComponent(critere)).then(function(response) {
                app.$refs.candidats.innerHTML = response.body;
                app.$refs.pagination.pagePrecedenteChargee(index);
            }, function(response) {
                // erreur
            });
        }
    }
});