"use strict";

$(document).ready(function () {

    var body = $("body");
    var selecteurSecteursActivites = $("#js-secteursActivites-selecteur");
    var selecteurMetiers = $("#js-metiers-selecteur");
    var selecteurDepartements = $("#js-departements-selecteur");

    app.chargerPage(0); // premiere page

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
            csrfToken: jsData.csrfToken,
            nbCandidats: jsData.nbCandidats,
            nbCandidatsParPage: 25,
            frequences: [ // FIXME: reçues du back
                {value: 'Quotidienne', label: 'Chaque jour'},
                {value: 'Hebdomadaire', label: 'Chaque semaine'}
            ],
            alertes: jsData.alertes,
            secteurActivite: jsData.secteurActivite,
            secteursActivites: jsData.secteursActivites,
            departement: jsData.departement,
            departements: jsData.departements,
            metier: jsData.metier,
            labelTousLesMetiers: 'Tous les métiers'
        }
    },
    computed: {
        pagesInitiales: function() {
            return this.calculerPages();
        },
        metiers: function() {
            var secteurActivite = this.secteurActivite;
            if (secteurActivite !== undefined && secteurActivite !== '') {
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
        titreCompteurResultats: function() {
            if (this.nbCandidats === 0) {
                return "Nous n'avons pas de candidats à vous proposer avec ces critères";
            } else {
                if (this.metier !== undefined && this.metier !== '') {
                    return "<b>" + this.getIntituleCandidats(this.nbCandidats) + " pour ce métier</b><br/>" + this.getSuffixeCandidats(this.nbCandidats);
                } else if (this.secteurActivite !== undefined && this.secteurActivite !== '') {
                    return "<b>" + this.getIntituleCandidats(this.nbCandidats) + " pour ce secteur d'activité</b><br/>" + this.getSuffixeCandidats(this.nbCandidats);
                } else if (this.departement !== undefined && this.departement !== '') {
                    return "<b>" + this.getIntituleCandidats(this.nbCandidats) + " pour ce département</b><br/>" + this.getSuffixeCandidats(this.nbCandidats);
                } else {
                    return "<b>" + this.getIntituleCandidats(this.nbCandidats) + " perspectives</b><br/>" + this.getSuffixeCandidats(this.nbCandidats);
                }
            }
        },
        getIntituleCandidats: function(nbCandidats) {
            return nbCandidats === 1 ? "1 candidat" : nbCandidats + " candidats";
        },
        getSuffixeCandidats: function(nbCandidats) {
            return nbCandidats === 1 ? "est validé par la Méthode de Recrutement par Simulation" : "sont validés par la Méthode de Recrutement par Simulation";
        },
        initialiserTableau: function() {
            app.$refs.pagination.modifierPagination(this.pagesInitiales);
            app.chargerPage(0); // premiere page
        },
        rechercherCandidats: function() {
            var formData = [
                {name: 'csrfToken', value: this.csrfToken},
                {name: "secteurActivite", value: this.secteurActivite},
                {name: "metier", value: this.metier},
                {name: "codeDepartement", value: this.departement}
            ];
            return $.ajax({
                type: "POST",
                url: "/recruteur/recherche",
                data: formData,
                dataType: 'json'
            }).done(function (response) {
                app.nbCandidats = response.nbCandidats;
                $("#js-resultatsRecherche").html(response.html);
                app.initialiserTableau();
                app.titreCompteurResultats();
            });
        },
        onSecteurActiviteModifie: function() {
            this.metier = '';
            this.rechercherCandidats();
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
            this.chargerPage(critere);
            app.$refs.pagination.pageSuivanteChargee(0, '');
        },
        chargerPagePrecedente: function(critere, index) {
            this.chargerPage(critere);
            app.$refs.pagination.pagePrecedenteChargee(index);
        },
        calculerPages: function() {
            var nbPages = Math.ceil(this.nbCandidats / this.nbCandidatsParPage);
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
            if (this.metier !== "") {
                intitule += this.metiers.find(function(m) {
                    return m.codeROME === app.metier;
                }).label;
            } else if (this.secteurActivite !== "") {
                intitule += this.secteursActivites.find(function(s) {
                    return s.code === app.secteurActivite;
                }).label;
            } else {
                intitule += "Candidats";
            }
            if (this.departement !== "") {
                intitule += " en " + this.departements.find(function(f) {
                    return f.code === app.departement;
                }).label;
            }
            return intitule;
        },
        creerAlerte: function(frequence) {
            var formData = [
                {name: "csrfToken", value: this.csrfToken},
                {name: "secteurActivite", value: this.secteurActivite},
                {name: "metier", value: this.metier},
                {name: "codeDepartement", value: this.departement},
                {name: "frequence", value: frequence}
            ];
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
                            codeSecteurActivite: app.secteurActivite,
                            codeROME: app.metier,
                            codeDepartement: app.departement
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
            this.departement = criteres.codeDepartement;

            this.rechercherCandidats();
        }
    }
});