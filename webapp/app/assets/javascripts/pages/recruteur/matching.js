"use strict";

$(document).ready(function () {
    var secteursActivites = Object.freeze({
        'A': ['A1401'],
        'G': ['G1603', 'G1803'],
        'F': ['F1602', 'F1703', 'F1301'],
        'D': ['D1507', 'D1505', 'D1106', 'N1105'],
        'K': ['K1302', 'K1304', 'K2204'],
        'B': ['B1802', 'H2402'],
        'H': ['H2202', 'H2913', 'H3203', 'H3302', 'N1105']
    });
    var metiers = Object.freeze({
        'A1401': 'Aide agricole',
        'B1802': 'Réalisation d\'articles',
        'D1106': 'Vente',
        'D1505': 'Caisse',
        'D1507': 'Mise en rayon',
        'F1301': 'Conduite d’engins',
        'F1602': 'Électricité',
        'F1703': 'Maçonnerie',
        'G1603': 'Personnel polyvalent',
        'G1803': 'Service',
        'H2101': 'Découpe de viande',
        'H2102': 'Conduite de machines',
        'H2202': 'Conduite de machines',
        'H2402': 'Mécanicien en confection',
        'H2913': 'Soudage',
        'H3203': 'Fabrication de pièces',
        'H3302': 'Tri et emballage',
        'K1302': 'Aide aux personnes âgées',
        'K1304': 'Aide à domicile',
        'K2204': 'Nettoyage de locaux',
        'N1103': 'Préparation de commandes',
        'N1105': 'Manutention'
    });

    var body = $("body");
    var criteresRechercheForm = $("#criteresRechercheForm");
    var selecteurSecteursActivites = $("#js-secteursActivites-selecteur");
    var selecteurMetiers = $("#js-metiers-selecteur");
    var htmlTousLesMetiers = selecteurMetiers.html();
    var resultatsRecherche = $("#js-resultatsRecherche");
    var compteurResultat = $(".compteurResultats-compteur");

    selecteurSecteursActivites.change(function () {
        var secteurActivite = $(this).val();
        rechercherCandidats().always(function () {
            selecteurMetiers.empty();
            if (secteurActivite !== '') {
                var codesMetiers = secteursActivites[secteurActivite];
                selecteurMetiers.append(
                    $('<option>')
                        .val("")
                        .text("Tous les métiers du secteur"));
                for (var i = 0; i < codesMetiers.length; i++) {
                    selecteurMetiers.append(
                        $('<option>')
                            .val(codesMetiers[i])
                            .text(metiers[codesMetiers[i]])
                    )
                }
            } else {
                selecteurMetiers.html(htmlTousLesMetiers);
            }
            // FIXME : nombre de résultats retournés (prise en compte d'une éventuelle pagination)
            var nbResultats = $(".resultatsRecherche tbody tr").length;
            compteurResultat.text(nbResultats + " candidat(s) pour ce secteur");
        });
    });

    selecteurMetiers.change(function () {
        rechercherCandidats().always(function () {
            // FIXME : nombre de résultats retournés (prise en compte d'une éventuelle pagination)
            var nbResultats = $(".resultatsRecherche tbody tr").length;
            compteurResultat.text(nbResultats + " candidat(s) pour ce métier");
        });
    });

    function rechercherCandidats() {
        return $.ajax({
            type: "POST",
            url: "/recruteur/matching-candidats",
            data: criteresRechercheForm.serializeArray(),
            dataType: 'text'
        }).done(function (data) {
            resultatsRecherche.html(data);
        });
    }

    body.on("click", ".js-boutonCandidat", function () {
        var bouton = $(this);
        bouton.toggle();
        bouton.next(".js-infoCandidat").toggle();
    });
    body.on("click", ".js-infoCandidat", function () {
        var bouton = $(this);

        bouton.next(".js-copiePressePapier").get(0).select();
        document.execCommand("copy");

        bouton.find("~ .js-infoBulle").each(function () {
            $(this).show().delay(1000).hide(10);
        });
    });
});