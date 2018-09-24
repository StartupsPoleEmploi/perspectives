"use strict";

// FIXME : en faire un vrai composant pour une meilleure réutilisation
$(document).ready(function() {
    var body = $("body");
    var resultatsConteneur = $("#js-listeResultats");
    var conteneurPagesPrecedentes = $("#js-pagesPrecedentes");
    var criterePremierePage = $("#js-criterePremierePage");
    var criterePageSuivante = $("#js-criterePageSuivante"); // contient la valeur pour accéder à la prochaine page
    var pagesPrecedentes = [];

    // Initialisation
    modifierPagesPrecedentes(criterePremierePage.val());
    modifierPageSuivante();

    body.on("click", ".js-pageSuivante", function (e) {
        e.preventDefault();
        var critere = criterePageSuivante.val();

        paginer(critere).done(function (data) {
            resultatsConteneur.html(data);

            modifierPagesPrecedentes(critere);
            modifierPageSuivante();
        });
    });
    body.on("click", ".js-pagePrecedente", function (e) {
        e.preventDefault();
        var numPage = $(this).text().trim();

        paginer(pagesPrecedentes[numPage - 1]).done(function (data) {
            resultatsConteneur.html(data);
        });
    });

    function paginer(critere) {
        return $.ajax({
            type: "GET",
            url: paginationBaseUrl + '/' + encodeURIComponent(critere),
            data: {},
            dataType: 'text'
        });
    }

    function modifierPagesPrecedentes(critere) {
        pagesPrecedentes.push(critere);
        conteneurPagesPrecedentes.append('<button class="bouton js-pagePrecedente" type="button">' + pagesPrecedentes.length + '</button>');
    }

    function modifierPageSuivante() {
        var dernierResultat = $("#js-dernierResultat");
        if (dernierResultat.val() !== undefined && dernierResultat.val() !== '') {
            criterePageSuivante.val(dernierResultat.val());
        } else {
            $(".js-pageSuivante").each(function() {
                $(this).hide();
            });
        }
    }
});