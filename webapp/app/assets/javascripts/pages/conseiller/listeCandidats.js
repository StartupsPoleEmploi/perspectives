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

var app = new Vue({
    el: '#listeCandidats',
    data: function() {
      return {
          pagesInitiales: jsData.pagesInitiales,
          nbCandidatsParPage: jsData.nbCandidatsParPage
      }
    },
    methods: {
        chargerPageSuivante: function(critere) {
            $.ajax({
                type: 'GET',
                url: '/conseiller/paginationCandidats' +
                '?dateInscription=' + encodeURIComponent(critere.dateInscription) +
                '&candidatId=' + encodeURIComponent(critere.candidatId),
                dataType: 'json'
            }).done(function (response) {
                app.$refs.candidats.innerHTML = response.html;
                app.$refs.pagination.pageSuivanteChargee(response.nbCandidats, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function(critere, index) {
            $.ajax({
                type: 'GET',
                url: '/conseiller/paginationCandidats' +
                '?dateInscription=' + encodeURIComponent(critere.dateInscription) +
                '&candidatId=' + encodeURIComponent(critere.candidatId),
                dataType: 'json'
            }).done(function (response) {
                app.$refs.candidats.innerHTML = response.html;
                app.$refs.pagination.pagePrecedenteChargee(index);
            });
        }
    }
});