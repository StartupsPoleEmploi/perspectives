"use strict";

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
var secteursActivitesLabels = Object.freeze({
    'A': 'Agriculture',
    'G': 'Hôtellerie restauration',
    'F': 'Bâtiment',
    'D': 'Commerce',
    'K': 'Services à la personne',
    'B': 'Textile',
    'H': 'Industrie',
    'N': 'Transport et logistique'
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
var departements = Object.freeze({
    '85': 'Vendée'
});
var frequences = [
    {value: 'Quotidienne', label: 'Chaque jour'},
    {value: 'Hebdomadaire', label: 'Chaque semaine'}
];

$(document).ready(function () {

    var body = $("body");
    var criteresRechercheForm = $("#criteresRechercheForm");
    var selecteurSecteursActivites = $("#js-secteursActivites-selecteur");
    var selecteurMetiers = $("#js-metiers-selecteur");
    var selecteurDepartements = $("#js-departements-selecteur");
    var htmlTousLesMetiers = selecteurMetiers.html();
    var resultatsRecherche = $("#js-resultatsRecherche");
    var titreCompteurResultats = $(".compteurResultats-titre");

    initialiserTableau();
    modifierPagination();
    modifierTitreCompteurResultats(selecteurSecteursActivites.val(), selecteurMetiers.val(), selecteurDepartements.val());

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
            url: "/recruteur/recherche",
            data: criteresRechercheForm.serializeArray(),
            dataType: 'text'
        }).done(function (data) {
            resultatsRecherche.html(data);
            initialiserTableau();
            modifierPagination();
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

    function modifierPagination() {
        app.$refs.pagination.modifierPagination(app.calculerPages());
        app.chargerPage(0); // premiere page
    }

    function initialiserTableau() {
        $(".js-infoCandidat").hide();
        $(".js-profilCandidat").hide();
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

    (function() {
        var commentaireRecruteur = $("#commentaireRecruteur");
        var formulaire = $("#commentaireListeCandidatsForm");
        var titre = $("#js-titreCommentaireRecruteur");
        var label = $("#js-labelCommentaireRecruteur");
        var actions = $("#js-commentaireActions");
        var commenterListeCandidats = $("#commenterListeCandidats");

        var initialiserFormulaire = function(labelCommentaire) {
            titre.text('Vous êtes satisfait(e)s de la liste qui vous est proposé ?');
            label.html(labelCommentaire);
            actions.hide();
            formulaire.show();
        };

        $("#js-satisfait").click(function() {
            initialiserFormulaire('<p>Parfait!</p>Si vous le souhaitez, vous pouvez nous envoyer des suggestions :');
        });
        $("#js-insatisfait").click(function() {
            initialiserFormulaire('Oups ! Nous sommes ouverts à vos retours !');
        });
        $("#js-envoyerCommentaire").click(function(e) {
            e.preventDefault();
            $("#js-commentaireSecteurActivite").val(selecteurSecteursActivites.val());
            $("#js-commentaireMetier").val(selecteurMetiers.val());
            $("#js-commentaireDepartement").val(selecteurDepartements.val());
            if (commentaireRecruteur.val() !== '') {
                $.ajax({
                    type: "POST",
                    url: "/recruteur/recherche/commenterListeCandidats",
                    data: formulaire.serializeArray(),
                    dataType: 'text'
                }).done(function () {
                    commenterListeCandidats.append('<p class="alert alert-success">Merci pour votre commentaire</p>');
                }).fail(function () {
                    commenterListeCandidats.append('<p class="alert alert-danger">Une erreur est survenue, veuillez réessayer ultérieurement</p>');
                }).always(function () {
                    formulaire.hide();
                    commenterListeCandidats.delay(2000).slideUp(400, function() {
                        window.location.hash = 'rechercheCandidat';
                    });
                });
            }
        });

        return {};
    })();
});

var app = new Vue({
    el: '#rechercheCandidat',
    data: function() {
        return {
            nbCandidatsParPage: 25,
            frequences: frequences
        }
    },
    computed: {
        pagesInitiales: function() {
            return this.calculerPages();
        }
    },
    methods: {
        chargerPageSuivante: function(critere) {
            this.chargerPage(critere);
            app.$refs.pagination.pageSuivanteChargee(0, '');
        },
        chargerPagePrecedente: function(critere, index) {
            this.chargerPage(critere);
            app.$refs.pagination.pagePrecedenteChargee(index);
        },
        calculerPages: function() {
            var nbCandidats = $(".listeResultatsRecherche-ligne").length;
            var nbPages = Math.ceil(nbCandidats / this.nbCandidatsParPage);
            var result = [];
            for (var i = 0; i < nbPages; i++) {
                result.push(i * this.nbCandidatsParPage);
            }
            return result;
        },
        chargerPage: function(critere) {
            var min = critere;
            var max = critere + this.nbCandidatsParPage;

            $(".listeResultatsRecherche-ligne").each(function(e) {
                $(this).toggle(e >= min && e < max);
            });
            $(".resultatsRecherche-titreConteneur").next(".listeResultatsRecherche").each(function() {
                var nbLignes = $(this).find(".listeResultatsRecherche-ligne:visible").length;
                $(this).prev(".resultatsRecherche-titreConteneur").toggle(nbLignes > 0);
            });
            $(".js-infoCandidat").hide();
            $(".js-profilCandidat").hide();
        },
        intituleAlerte: function() {
            var intitule = "";
            if (app.$refs.metier.value !== "") {
                intitule += metiers[app.$refs.metier.value];
            } else if (app.$refs.secteurActivite.value !== "") {
                intitule += secteursActivitesLabels[app.$refs.secteurActivite.value];
            } else {
                intitule += "Candidats";
            }
            if (app.$refs.departement.value !== "") {
                intitule += " en " + departements[app.$refs.departement.value];
            }
            return intitule;
        },
        creerAlerte: function(frequence) {
            var formData = $("#criteresRechercheForm").serializeArray();
            formData.push({name: "frequence", value: frequence});
            var intituleAlerte = this.intituleAlerte();
            var labelFrequence = app.$refs.alertesRecruteur.frequences.find(function(f) {
                return f.value === frequence;
            }).label;
            if (app.$refs.alertesRecruteur.alertes.findIndex(function(el) {
                return el.intitule === intituleAlerte && el.frequence === labelFrequence;
            }) !== -1) {
                app.$refs.alertesRecruteur.onErreur('Une alerte existe déjà avec ces critères');
            } else {
                $.ajax({
                    type: "POST",
                    url: "/recruteur/alerte",
                    data: formData,
                    dataType: 'text'
                }).done(function (alerteId) {
                    app.$refs.alertesRecruteur.onAlerteCree({
                        id: alerteId,
                        intitule: intituleAlerte,
                        frequence: labelFrequence,
                        criteres: {
                            codeSecteurActivite: app.$refs.secteurActivite.value,
                            codeROME: app.$refs.metier.value,
                            codeDepartement: app.$refs.departement.value
                        }
                    });
                }).fail(function () {
                    app.$refs.alertesRecruteur.onErreur('Une erreur est survenue, veuillez réessayer ultérieurement');
                });
            }
        },
        supprimerAlerte: function(alerteId) {
            $.ajax({
                type: "DELETE",
                url: "/recruteur/alerte/" + encodeURIComponent(alerteId) + "?csrfToken=" + $('input[name="csrfToken"]').val(),
                data: '',
                dataType: 'text'
            }).done(function () {
                app.$refs.alertesRecruteur.onAlerteSupprimee(alerteId);
            }).fail(function () {
                app.$refs.alertesRecruteur.onErreur('Une erreur est survenue, veuillez réessayer ultérieurement');
            });
        },
        selectionnerAlerte: function(criteres) {
            app.$refs.secteurActivite.value = criteres.codeSecteurActivite;
            app.$refs.departement.value = criteres.codeDepartement;

            if (criteres.codeROME !== '') {
                // FIXME : dupliqué
                var codesMetiers = secteursActivites[criteres.codeSecteurActivite];
                $("#js-metiers-selecteur").empty().append(
                    $('<option>')
                        .val("")
                        .text("Tous les métiers du secteur"));
                for (var i = 0; i < codesMetiers.length; i++) {
                    $("#js-metiers-selecteur").append(
                        $('<option>')
                            .val(codesMetiers[i])
                            .text(metiers[codesMetiers[i]])
                    )
                }
                app.$refs.metier.value = criteres.codeROME;
                $("#js-metiers-selecteur").change();
            } else if (criteres.codeSecteurActivite !== '') {
                app.$refs.metier.value = '';
                $("#js-secteursActivites-selecteur").change();
            } else {
                app.$refs.metier.value = '';
                $("#js-departements-selecteur").change();
            }
        }
    }
});