"use strict";

$(document).ready(function () {
    // FIXME : reçus depuis le back
    var secteursActivites = Object.freeze({
        'A': ['A1401'],
        'G': ['G1603', 'G1803'],
        'F': ['F1602', 'F1703', 'F1301'],
        'D': ['D1507', 'D1505', 'D1106'],
        'K': ['K1302', 'K1304', 'K2204'],
        'B': ['B1802'],
        'H': ['H2202', 'H2913', 'H3203', 'H3302', 'H2402'],
        'N': ['N1103', 'N1105', 'N4101']
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
        'N1105': 'Manutention',
        'N4101': 'Conduite de poids lourds'
    });

    var body = $("body");
    var criteresRechercheForm = $("#criteresRechercheForm");
    var selecteurSecteursActivites = $("#js-secteursActivites-selecteur");
    var selecteurMetiers = $("#js-metiers-selecteur");
    var selecteurDepartements = $("#js-departements-selecteur");
    var htmlTousLesMetiers = selecteurMetiers.html();
    var resultatsRecherche = $("#js-resultatsRecherche");
    var titreCompteurResultats = $(".compteurResultats-titre");

    initialiserTableau();
    modifierTitreCompteurResultats('', '', selecteurDepartements.val());

    selecteurSecteursActivites.change(function () {
        var secteurActivite = $(this).val();
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
        rechercherCandidats().done(function () {
            modifierTitreCompteurResultats(secteurActivite, '', selecteurDepartements.val());
        });
    });

    selecteurDepartements.change(function() {
        var codeDepartement = $(this).val();
        rechercherCandidats().done(function () {
            modifierTitreCompteurResultats(selecteurSecteursActivites.val(), selecteurMetiers.val(), codeDepartement);
        });
    });

    selecteurMetiers.change(function () {
        var metier = $(this).val();
        rechercherCandidats().done(function () {
            modifierTitreCompteurResultats(selecteurSecteursActivites.val(), metier, selecteurDepartements.val());
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
            initialiserTableau();
        });
    }

    function modifierTitreCompteurResultats(secteurActivite, metier, codeDepartement) {
        var nbResultats = $(".listeResultatsRecherche-ligne").length;
        if (nbResultats === 0) {
            titreCompteurResultats.html("Nous n'avons pas de candidats à vous proposer avec ces critères");
        } else {
            if (metier !== undefined && metier !== '') {
                titreCompteurResultats.html("<b>" + getIntituleCandidats(nbResultats) + " pour ce métier</b><br/>" + getSuffixeCandidats(nbResultats));
            } else if (secteurActivite !== undefined && secteurActivite !== '') {
                titreCompteurResultats.html("<b>" + getIntituleCandidats(nbResultats) + " pour ce secteur d'activité</b><br/>" + getSuffixeCandidats(nbResultats));
            } else if (codeDepartement !== undefined && codeDepartement !== '') {
                titreCompteurResultats.html("<b>" + getIntituleCandidats(nbResultats) + " pour ce département</b><br/>" + getSuffixeCandidats(nbResultats));
            } else {
                titreCompteurResultats.html("<b>" + getIntituleCandidats(nbResultats) + " perspectives</b><br/>" + getSuffixeCandidats(nbResultats));
            }
        }
    }

    function getIntituleCandidats(nbCandidats) {
        return nbCandidats === 1 ? "1 candidat" : nbCandidats + " candidats";
    }
    function getSuffixeCandidats(nbCandidats) {
        return nbCandidats === 1 ? "est validé par la Méthode de Recrutement par Simulation" : "sont validés par la Méthode de Recrutement par Simulation";
    }

    function initialiserTableau() {
        $(".js-infoCandidat").each(function() {
            $(this).hide();
        });
        $(".js-profilCandidat").each(function() {
            $(this).hide();
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

    body.on("click", "div[id^='js-ligne-']", function () {
        var ligne = $(this);
        var index = ligne.prop("id").substring("js-ligne-".length);
        var ligneProfil = $("#js-profilCandidat-" + index);

        if (ligneProfil.hasClass("profilCandidat--courant")) {
            ligneProfil.hide();
            ligneProfil.removeClass("profilCandidat--courant");
        } else {
            $(".profilCandidat--courant").each(function () {
                var ligneOuverte = $(this);
                ligneOuverte.hide();
                ligneOuverte.removeClass("profilCandidat--courant");
            });
            window.location.hash = ligne.attr("id");
            ligneProfil.slideDown(400, function() {
                ligneProfil.addClass("profilCandidat--courant");
            });
        }
    });

    var modaleVideoYoutube = $("#js-modaleVideo");
    var videoMRSYoutube = $("#js-videoMRSYoutube");
    var urlVideoMRS = "https://www.youtube.com/embed/VpQOnDxUQek?ecver=2&autoplay=1";

    modaleVideoYoutube.on("show.bs.modal", function () {
        videoMRSYoutube.attr("src", urlVideoMRS);
    });
    modaleVideoYoutube.on("hidden.bs.modal", function () {
        videoMRSYoutube.attr("src", "");
    });
});