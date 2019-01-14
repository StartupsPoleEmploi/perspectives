"use strict";

import pagination from '../../composants/pagination.js';
import alerteRecruteur from '../../composants/alerteRecruteur.js';
import places from 'places.js';
import { intituleAlerte, buildAlerte } from '../../domain/recruteur/alerte/alerteService.js';

$(document).ready(function () {

    var placesAutocomplete = places({
        appId: jsData.algoliaPlaces.appId,
        apiKey: jsData.algoliaPlaces.apiKey,
        container: document.querySelector('#js-localisation'),
        type: 'city',
        aroundLatLngViaIP: false,
        style: true,
        useDeviceLocation: false,
        language: 'fr',
        countries: ['fr']
    });
    placesAutocomplete.on('change', function(e) {
        app.localiser({
            label: e.suggestion.name,
            latitude: e.suggestion.latlng.lat,
            longitude: e.suggestion.latlng.lng
        });
    });
    placesAutocomplete.on('clear', function(e) {
        app.localiser({
            label: '',
            latitude: null,
            longitude: null
        });
    });

    var body = $("body");
    var selecteurSecteursActivites = $("#js-secteursActivites-selecteur");
    var selecteurMetiers = $("#js-metiers-selecteur");
    var inputLocalisation = $("#js-localisation");

    app.initialiserTableau();

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
        var profilCandidat = $("#js-profilCandidat-" + index);
        var profilFerme = ligne.find("img.voirProfil--ferme");
        var profilOuvert = ligne.find("img.voirProfil--ouvert");

        if (profilCandidat.hasClass("profilCandidat--courant")) {
            profilCandidat.hide();
            profilCandidat.removeClass("profilCandidat--courant");
            profilFerme.show();
            profilOuvert.hide();
        } else {
            $(".profilCandidat--courant").each(function () {
                var profilCandidatCourant = $(this);
                var indexProfilCourant = profilCandidatCourant.prop("id").substring("js-profilCandidat-".length);
                var ligneCandidatCourant = $("#js-ligne-" + indexProfilCourant);
                var profilFermeCandidatCourant = ligneCandidatCourant.find("img.voirProfil--ferme");
                var profilOuvertCandidatCourant = ligneCandidatCourant.find("img.voirProfil--ouvert");

                profilCandidatCourant.hide();
                profilCandidatCourant.removeClass("profilCandidat--courant");
                profilFermeCandidatCourant.show();
                profilOuvertCandidatCourant.hide();
            });
            profilFerme.hide();
            profilOuvert.show();
            profilCandidat.slideDown(400, function() {
                profilCandidat.addClass("profilCandidat--courant");
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
            $("#js-commentaireLocalisation").val(inputLocalisation.val());
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
            csrfToken: jsData.csrfToken,
            nbCandidatsParPage: jsData.nbCandidatsParPage,
            pagesInitiales: jsData.pagesInitiales,
            alertes: jsData.alertes.map(function(alerte) {
                return buildAlerte(alerte, jsData.metiers, jsData.secteursActivites);
            }),
            secteurActivite: jsData.secteurActivite !== undefined && jsData.secteurActivite !== null ? jsData.secteurActivite : '',
            secteursActivites: jsData.secteursActivites,
            metier: jsData.metier !== undefined && jsData.metier !== null ? jsData.metier : '',
            localisation: jsData.localisation !== undefined && jsData.localisation !== null ? jsData.localisation : {
                label: '',
                latitude: null,
                longitude: null
            },
            labelTousLesMetiers: 'Tous les métiers',
            titreCompteurResultats: null
        }
    },
    beforeMount: function() {
        this.titreCompteurResultats = this.getTitreCompteurResultats(jsData.nbCandidatsTotal);
    },
    computed: {
        metiers: function() {
            var secteurActivite = this.secteurActivite;
            if (secteurActivite !== null && secteurActivite !== '') {
                this.labelTousLesMetiers = 'Tous les métiers du secteur';
                return this.secteursActivites.find(function(s) {
                    return s.code === secteurActivite;
                }).metiers;
            } else {
                this.labelTousLesMetiers = 'Tous les métiers';
                return jsData.metiers;
            }
        }
    },
    methods: {
        getTitreCompteurResultats: function(nbCandidats) {
            if (nbCandidats === 0) {
                return "Nous n'avons pas de candidats à vous proposer avec ces critères";
            } else {
                if (this.metier !== undefined && this.metier !== '') {
                    return "<b>" + this.getIntituleCandidats(nbCandidats) + " pour ce métier</b> " + this.getSuffixeCandidats(nbCandidats);
                } else if (this.secteurActivite !== undefined && this.secteurActivite !== '') {
                    return "<b>" + this.getIntituleCandidats(nbCandidats) + " pour ce secteur d'activité</b> " + this.getSuffixeCandidats(nbCandidats);
                } else if (this.localisation !== null && this.localisation.label !== '') {
                    return "<b>" + this.getIntituleCandidats(nbCandidats) + " à " + this.localisation.label + "</b> " + this.getSuffixeCandidats(nbCandidats);
                } else {
                    return "<b>" + this.getIntituleCandidats(nbCandidats) + " perspectives</b> " + this.getSuffixeCandidats(nbCandidats);
                }
            }
        },
        getIntituleCandidats: function(nbCandidats) {
            return nbCandidats === 1 ? "1 candidat" : nbCandidats + " candidats";
        },
        getSuffixeCandidats: function(nbCandidats) {
            return (nbCandidats === 1 ? "est validé" : "sont validés") + " par la <abbr title='Méthode de Recrutement par Simulation' data-toggle='modal' data-target='#js-modaleVideo'>MRS</abbr>";
        },
        localiser: function(localisation) {
            this.localisation = localisation;
        },
        rechercherCandidats: function() {
            var formData = [
                {name: "csrfToken", value: this.csrfToken},
                {name: "secteurActivite", value: this.secteurActivite},
                {name: "metier", value: this.metier},
                {name: "coordonnees.latitude", value: this.localisation !== null ? this.localisation.latitude: null},
                {name: "coordonnees.longitude", value: this.localisation !== null ? this.localisation.longitude: null}
            ];
            return $.ajax({
                type: "POST",
                url: "/recruteur/recherche",
                data: formData,
                dataType: 'json'
            }).done(function (response) {
                app.$refs.resultatsRecherche.innerHTML = response.html;
                app.initialiserTableau();
                app.$refs.pagination.modifierPagination(response.pages);
                app.titreCompteurResultats = app.getTitreCompteurResultats(response.nbCandidatsTotal);
            });
        },
        onSecteurActiviteModifie: function() {
            this.metier = '';
        },
        modifierMetiersPourSecteur: function() {
            if (this.secteurActivite !== '') {
                this.labelTousLesMetiers = 'Tous les métiers du secteur';
                this.metiers = this.secteursActivites.find(function(s) {
                    return s.code === app.secteurActivite;
                }).metiers;
            } else {
                this.labelTousLesMetiers = 'Tous les métiers';
                this.metiers = jsData.metiers;
            }
        },
        chargerPageSuivante: function(critere) {
            this.paginerCandidats(critere).done(function (response) {
                app.$refs.resultatsRecherche.innerHTML = response.html;
                app.initialiserTableau();
                app.$refs.pagination.pageSuivanteChargee(response.nbCandidats, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function(critere, index) {
            this.paginerCandidats(critere).done(function (response) {
                app.$refs.resultatsRecherche.innerHTML = response.html;
                app.initialiserTableau();
                app.$refs.pagination.pagePrecedenteChargee(index);
            });
        },
        paginerCandidats: function(critere) {
            var formData = [
                {name: "csrfToken", value: this.csrfToken},
                {name: "secteurActivite", value: this.secteurActivite},
                {name: "metier", value: this.metier},
                {name: "coordonnees.latitude", value: this.localisation !== null ? this.localisation.latitude: null},
                {name: "coordonnees.longitude", value: this.localisation !== null ? this.localisation.longitude: null},
                {name: "pagination.score", value: critere.score},
                {name: "pagination.dateInscription", value: critere.dateInscription},
                {name: "pagination.candidatId", value: critere.candidatId}
            ];
            return $.ajax({
                type: 'POST',
                url: '/recruteur/recherche',
                data: formData,
                dataType: 'json'
            })
        },
        initialiserTableau: function() {
            $(".js-infoCandidat").hide();
            $(".js-profilCandidat").hide();
        },
        creerAlerte: function(frequence) {
            var formData = [
                {name: "csrfToken", value: this.csrfToken},
                {name: "secteurActivite", value: this.secteurActivite},
                {name: "metier", value: this.metier},
                {name: "localisation.label", value: this.localisation !== null ? this.localisation.label: null},
                {name: "localisation.latitude", value: this.localisation !== null ? this.localisation.latitude: null},
                {name: "localisation.longitude", value: this.localisation !== null ? this.localisation.longitude: null},
                {name: "frequence", value: frequence}
            ];
            var intituleAlerte = intituleAlerte({
                metier: this.metier,
                secteurActivite: this.secteurActivite,
                localisation: this.localisation
            }, this.metiers, this.secteursActivites);
            var labelFrequence = app.$refs.alertesRecruteur.frequences.find(function(f) {
                return f.value === frequence;
            }).label;

            if (this.secteurActivite === '' && this.metier === '' && (this.localisation === null || this.localisation.latitude === null || this.localisation.longitude === null)) {
                app.$refs.alertesRecruteur.onErreur('Choisissez au moins un critère pour créer une alerte');
            } else if (app.$refs.alertesRecruteur.alertes.findIndex(function(el) {
                return el.intitule === intituleAlerte && el.frequence === labelFrequence;
            }) !== -1) { // FIXME : ne pas chercher sur l'intitulé : pas solide
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
                            codeSecteurActivite: app.secteurActivite,
                            codeROME: app.metier,
                            localisation: app.localisation
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
                url: "/recruteur/alerte/" + encodeURIComponent(alerteId) + "?csrfToken=" + this.csrfToken,
                data: '',
                dataType: 'text'
            }).done(function () {
                app.$refs.alertesRecruteur.onAlerteSupprimee(alerteId);
            }).fail(function () {
                app.$refs.alertesRecruteur.onErreur('Une erreur est survenue, veuillez réessayer ultérieurement');
            });
        },
        selectionnerAlerte: function(criteres) {
            this.secteurActivite = criteres.codeSecteurActivite;
            this.metier = criteres.codeROME;
            this.localisation = criteres.localisation;

            this.rechercherCandidats();
        }
    }
});