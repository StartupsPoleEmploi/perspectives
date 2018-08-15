"use strict";

$(document).ready(function () {
    var secteursActivites = Object.freeze({
        'A': ['A1401'],
        'G': ['G1603', 'G1803'],
        'F': ['F1602', 'F1703', 'F1301'],
        'D': ['D1507', 'D1505', 'D1106'],
        'K': ['K1302', 'K1304', 'K2204'],
        'B': ['B1802', 'H2402'],
        'H': ['H2202', 'H2913', 'H3203', 'H3302'],
        'N': ['N1105']
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
    var titreCompteurResultats = $(".compteurResultats-titre");

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
            var nbResultats = $(".resultatsRecherche tbody tr").length;
            if (nbResultats === 0) {
                titreCompteurResultats.html("Nous n'avons pas de candidats à vous proposer avec ces critères");
            } else if (nbResultats === 1) {
                if (secteurActivite !== '') {
                    titreCompteurResultats.html("<b>1 candidat intéressé pour ce secteur d'activité</b><br/>a validé la Méthode de Recrutement par Simulation");
                } else {
                    titreCompteurResultats.html("<b>1 candidat perspectives</b><br/>a validé la Méthode de Recrutement par Simulation");
                }
            } else {
                if (secteurActivite !== '') {
                    titreCompteurResultats.html("<b>" + nbResultats + " candidats intéréssés pour ce secteur d'activité</b><br/>ont validé la Méthode de Recrutement par Simulation");
                } else {
                    titreCompteurResultats.html("<b>" + nbResultats + " candidats perspectives</b><br/>ont validé la Méthode de Recrutement par Simulation");
                }
            }
        });
    });

    selecteurMetiers.change(function () {
        rechercherCandidats().always(function () {
            var nbResultats = $(".resultatsRecherche tbody tr").length;
            if (nbResultats === 0) {
                titreCompteurResultats.html("Nous n'avons pas de candidats à vous proposer avec ces critères");
            } else if (nbResultats === 1) {
                titreCompteurResultats.html("<b>1 candidat intéressé pour ce métier</b><br/>a validé la Méthode de Recrutement par Simulation");
            } else {
                titreCompteurResultats.html("<b>" + nbResultats + " candidats intéréssés pour ce métier</b><br/>ont validé la Méthode de Recrutement par Simulation");
            }
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