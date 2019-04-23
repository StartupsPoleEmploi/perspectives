import Vue from 'vue';
import $ from 'jquery';
import places from 'places.js';
import 'bootstrap/js/dist/modal';
import pagination from '../../composants/pagination.js';
import '../../commun/filters.js';

$(document).ready(function () {
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
            $("#js-commentaireSecteurActivite").val($("#js-secteursActivites-selecteur option:selected").text());
            $("#js-commentaireMetier").val($("#js-metiers-selecteur option:selected").text());
            $("#js-commentaireLocalisation").val($("#js-localisation").val());
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
            candidatCourant: null,
            secteursActivites: jsData.secteursActivites,
            secteurActivite: '',
            metier: '',
            secteurActiviteChoisi: null,
            metierChoisi: null,
            localisation: {
                label: '',
                latitude: null,
                longitude: null
            },
            resultatRecherche:  Object.assign({
                    candidats: [],
                    nbCandidats: 0,
                    nbCandidatsTotal: 0,
                    pages: []
                }, jsData.resultatRecherche),
            sectionsCandidats: [{
                titre: null,
                candidats: Object.assign([], jsData.resultatRecherche.candidats)
            }],
            display: {
                numTelephones: []
            }
        }
    },
    mounted: function() {
        var self = this;
        var placesAutocomplete = places({
            appId: jsData.algoliaPlacesConfig.appId,
            apiKey: jsData.algoliaPlacesConfig.apiKey,
            container: document.querySelector('#js-localisation'),
            type: 'city',
            aroundLatLngViaIP: false,
            style: true,
            useDeviceLocation: false,
            language: 'fr',
            countries: ['fr']
        });
        placesAutocomplete.on('change', function(e) {
            self.localisation = {
                label: e.suggestion.name,
                latitude: e.suggestion.latlng.lat,
                longitude: e.suggestion.latlng.lng
            };
        });
        placesAutocomplete.on('clear', function(e) {
            self.localisation = {
                label: '',
                latitude: null,
                longitude: null
            };
        });
    },
    computed: {
        metiers: function() {
            var self = this;
            if (this.secteurActivite !== null && this.secteurActivite !== '') {
                return this.secteursActivites.find(function(s) {
                    return s.code === self.secteurActivite;
                }).metiers;
            } else {
                return [];
            }
        }
    },
    methods: {
        secteurActiviteParCode: function(codeSecteur) {
             return this.secteursActivites.find(function(s) {
                 return s.code === codeSecteur;
             });
        },
        metiersRecherchesParSecteur: function(candidat) {
            return candidat.metiersRecherches.reduce(function (acc, metier) {
                var key = metier.codeROME.charAt(0);
                acc[key] = acc[key] || [];
                acc[key].push(metier);
                return acc;
            }, {});
        },
        toggleProfil: function (candidatId) {
            if (this.candidatCourant === candidatId) {
                this.candidatCourant = null;
            } else {
                this.candidatCourant = candidatId;
            }
        },
        estProfilCourant: function(candidatId) {
          return candidatId === this.candidatCourant;
        },
        interessePar: function(candidat) {
            var self = this;
            var metiersRecherchesParSecteur = this.metiersRecherchesParSecteur(candidat);

            if (this.metierChoisi) {
                var metier = candidat.metiersRecherches.filter(function(m) {
                    return m.codeROME === self.metierChoisi;
                });
                if (candidat.metiersValides.filter(function(m) {
                    return m.codeROME === self.metierChoisi;
                }).length == 0 && metier.length > 0) {
                    return metier[0].label;
                }
                return '';
            } else if (this.secteurActiviteChoisi) {
                if (metiersRecherchesParSecteur.hasOwnProperty(this.secteurActiviteChoisi)) {
                    return self.secteurActiviteParCode(this.secteurActiviteChoisi).label;
                }
                return '';
            } else {
                return Object.keys(metiersRecherchesParSecteur).map(function(k) {
                    return self.secteurActiviteParCode(k).label;
                }).join(', ');
            }
        },
        detailInteressePar: function(candidat) {
            var self = this;
            var metiersRecherchesParSecteur = this.metiersRecherchesParSecteur(candidat);

            if (this.metierChoisi) {
                var secteurActiviteChoisi = this.metierChoisi.charAt(0);
                if (metiersRecherchesParSecteur.hasOwnProperty(secteurActiviteChoisi)) {
                    return [self.secteurActiviteParCode(secteurActiviteChoisi).label + ' : ' + metiersRecherchesParSecteur[secteurActiviteChoisi].map(function(m) {
                        return m.label;
                    })];
                }
                return [];
            } else if (this.secteurActiviteChoisi) {
                if (metiersRecherchesParSecteur.hasOwnProperty(this.secteurActiviteChoisi)) {
                    return [self.secteurActiviteParCode(this.secteurActiviteChoisi).label + ' : ' + metiersRecherchesParSecteur[this.secteurActiviteChoisi].map(function(m) {
                        return m.label;
                    })];
                }
                return [];
            } else {
                return Object.keys(metiersRecherchesParSecteur).map(function(k) {
                    return self.secteurActiviteParCode(k).label + ' : ' + metiersRecherchesParSecteur[k].map(function(m) {
                        return m.label;
                    }).join(', ');
                });
            }
        },
        mobilite: function(candidat) {
            return candidat.rayonRecherche !== undefined ? (candidat.rayonRecherche.value + 'km autour de') : '';
        },
        afficherNumeroTelephone: function(candidatId) {
            this.display.numTelephones.push(candidatId);
        },
        copierNumeroTelephone(candidatId) {
            document.querySelector("#js-numeroTelephone-" + candidatId).select();
            document.execCommand("copy");

            $("#js-infoBulle-" + candidatId).show().delay(1000).hide(10);
        },
        rechercherCandidats: function(pagination) {
            var formData = [
                {name: "csrfToken", value: this.csrfToken},
                {name: "secteurActivite", value: this.secteurActivite},
                {name: "metier", value: this.metier},
                {name: "coordonnees.latitude", value: this.localisation !== null ? this.localisation.latitude: null},
                {name: "coordonnees.longitude", value: this.localisation !== null ? this.localisation.longitude: null}
            ];
            if (pagination) {
                formData.push({name: "pagination.score", value: pagination.score});
                formData.push({name: "pagination.dateInscription", value: pagination.dateInscription});
                formData.push({name: "pagination.candidatId", value: pagination.candidatId});
            }
            app.candidatCourant = null;

            return $.ajax({
                type: "POST",
                url: "/recruteur/recherche",
                data: formData,
                dataType: 'json'
            }).done(function (response) {
                app.resultatRecherche = response;
                app.secteurActiviteChoisi = app.secteurActivite;
                app.metierChoisi = app.metier;
                app.modifierSectionsCandidats(response.candidats);
                if (!pagination) {
                    app.display.numTelephones = [];
                    app.$refs.pagination.modifierPagination(response.pages);
                }
            });
        },
        modifierSectionsCandidats: function(candidats) {
            var self = this;
            var candidatsValides = [];
            var candidatsInteresses = [];
            if (self.metierChoisi) {
                var secteurActivite = self.secteurActiviteParCode(self.secteurActiviteChoisi);
                var metier = secteurActivite.metiers.filter(function(m) {
                    return m.codeROME === self.metierChoisi;
                })[0];
                candidats.forEach(function(c) {
                    if (c.metiersValidesRecherches.filter(function(m) {
                        return m.codeROME === self.metierChoisi;
                    }).length !== 0) {
                        candidatsValides.push(c);
                    } else {
                        candidatsInteresses.push(c);
                    }
                });
                self.sectionsCandidats = [
                    {
                        titre: self.getTitreSection('<b>Candidat validé</b> sur le métier "' + metier.label + '"', candidatsValides),
                        candidats: candidatsValides
                    }, {
                        titre: self.getTitreSection('<b>Candidat intéréssé</b> par le métier "' + metier.label + '"', candidatsInteresses),
                        candidats: candidatsInteresses
                    },
                ]
            } else if (self.secteurActiviteChoisi) {
                var secteurActivite = self.secteurActiviteParCode(self.secteurActiviteChoisi);
                candidats.forEach(function(c) {
                    if (c.metiersValidesRecherches.filter(function(m) {
                        return m.codeROME.charAt(0) === self.secteurActiviteChoisi;
                    }).length !== 0) {
                        candidatsValides.push(c);
                    } else {
                        candidatsInteresses.push(c);
                    }
                });
                self.sectionsCandidats = [
                    {
                        titre: self.getTitreSection('<b>Candidat validé</b> dans le secteur "' + secteurActivite.label + '"', candidatsValides),
                        candidats: candidatsValides
                    }, {
                        titre: self.getTitreSection('<b>Candidat intéréssé</b> par le secteur "' + secteurActivite.label + '"', candidatsInteresses),
                        candidats: candidatsInteresses
                    },
                ]
            } else {
                self.sectionsCandidats = [{
                    titre: null,
                    candidats: candidats
                }]
            }
        },
        getTitreSection: function(titreSingulier, candidats) {
            if (candidats.length === 1) {
                return titreSingulier;
            } else {
                return titreSingulier.replace(/Candidat/gi, 'Candidats').replace(/validé/gi, 'validés').replace(/intéréssé/gi, 'intéréssés');
            }
        },
        onSecteurActiviteModifie: function() {
            this.metier = '';
        },
        chargerPageSuivante: function(critere) {
            this.rechercherCandidats(critere).done(function (response) {
                app.$refs.pagination.pageSuivanteChargee(response.nbCandidats, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function(critere, index) {
            this.rechercherCandidats(critere).done(function (response) {
                app.$refs.pagination.pagePrecedenteChargee(index);
            });
        }
    }
});