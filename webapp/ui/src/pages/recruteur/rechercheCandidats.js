import Vue from 'vue';
import $ from 'jquery';
import places from 'places.js';
import 'bootstrap/js/dist/modal';
import Pagination from '../../composants/Pagination.vue';
import '../../commun/filters.js';
import listeTempsTravail from "../../domain/candidat/tempsTravail";
import niveauxLangues from "../../domain/candidat/niveauxLangues";

Vue.filter('dateExperience', function (value) {
    return new Date(value).toLocaleString('fr-FR', {month: 'long', year: "numeric"});
});

var app = new Vue({
    el: '#rechercheCandidats',
    components: {
        'pagination': Pagination
    },
    data: function () {
        return {
            // FIXME : reorganisation des variables par contexte (détail, recherche, etc.)
            csrfToken: jsData.csrfToken,
            nbCandidatsParPage: jsData.nbCandidatsParPage,
            nbSavoirFaireParPage: 10,
            candidatCourant: null,
            indexPaginationSavoirFaireCandidat: 1,
            indexNavigationCandidat: 0,
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
            localisationChoisie: null,
            resultatRecherche: Object.assign({
                candidats: [],
                nbCandidats: 0,
                nbCandidatsTotal: 0,
                pages: []
            }, jsData.resultatRecherche),
            display: {
                modaleDetailCandidat: false,
                chargement: false,
                contact: false,
                numeroTelephoneCopie: false,
                habiletesParMetier: null,
                ongletsDetailCandidat: null
            }
        }
    },
    mounted: function () {
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
            countries: ['fr'],
            templates: {
                value: function(suggestion) {
                    return suggestion.name;
                }
            }
        });
        placesAutocomplete.on('change', function (e) {
            self.localisation = {
                label: e.suggestion.name,
                latitude: e.suggestion.latlng.lat,
                longitude: e.suggestion.latlng.lng
            };
        });
        placesAutocomplete.on('clear', function (e) {
            self.localisation = {
                label: '',
                latitude: null,
                longitude: null
            };
        });

        window.location = '#';
        var modaleDetail = $('#detailCandidat');
        modaleDetail.on('show.bs.modal', function () {
            self.display.modaleDetailCandidat = true;
            window.location = '#detailCandidat';
        }).on('hide.bs.modal', function () {
            self.display.modaleDetailCandidat = false;
            window.location = '#';
        });
        window.onpopstate = function (event) {
            if (self.display.modaleDetailCandidat && window.location.href.endsWith('#')) {
                modaleDetail.modal('hide');
            }
            if (!self.display.modaleDetailCandidat && window.location.href.endsWith('#detailCandidat')) {
                modaleDetail.modal('show');
            }
        };
    },
    computed: {
        metiers: function () {
            var self = this;
            if (this.secteurActivite !== null && this.secteurActivite !== '') {
                return this.secteursActivites.find(function (s) {
                    return s.code === self.secteurActivite;
                }).metiers;
            } else {
                return [];
            }
        },
        sectionsCandidats: function() {
            var self = this;
            var candidatsValides = [];
            var candidatsInteresses = [];
            if (self.metierChoisi) {
                var metiersSecteur = self.secteurActiviteParCode(self.secteurActiviteChoisi).metiers;
                var labelMetier = metiersSecteur.find(function(m) {
                    return m.codeROME === self.metierChoisi;
                }).label;
                self.resultatRecherche.candidats.forEach(function(c) {
                    if (c.metiersValidesRecherches.filter(function(m) {
                        return m.codeROME.indexOf(self.metierChoisi) !== -1;
                    }).length !== 0) {
                        candidatsValides.push(c);
                    } else {
                        candidatsInteresses.push(c);
                    }
                });
                var titreSectionInteresses = '';
                if (self.$refs.pagination.getPageCourante() === 1 && candidatsValides.length === 0) {
                    titreSectionInteresses = self.titreSansCandidatsValides() + self.suffixeMetier(labelMetier) + this.suffixeVille();
                } else {
                    titreSectionInteresses = 'D\'autres candidats recherchent ' + self.suffixeMetier(labelMetier);
                }
                return [
                    {
                        titre: self.titreSectionValides() + self.suffixeMetier(labelMetier),
                        candidats: candidatsValides
                    }, {
                        titre: titreSectionInteresses,
                        candidats: candidatsInteresses
                    },
                ]
            } else if (self.secteurActiviteChoisi) {
                var labelSecteurActivite = self.secteurActiviteParCode(self.secteurActiviteChoisi).label;
                self.resultatRecherche.candidats.forEach(function(c) {
                    if (c.metiersValidesRecherches.filter(function(m) {
                        return m.codeROME.charAt(0) === self.secteurActiviteChoisi;
                    }).length !== 0) {
                        candidatsValides.push(c);
                    } else {
                        candidatsInteresses.push(c);
                    }
                });
                var titreSectionInteresses = '';
                if (self.$refs.pagination.getPageCourante() === 1 && candidatsValides.length === 0) {
                    titreSectionInteresses = self.titreSansCandidatsValides() + self.suffixeSecteur(labelSecteurActivite) + this.suffixeVille();
                } else {
                    titreSectionInteresses = 'D\'autres candidats recherchent ' + self.suffixeSecteur(labelSecteurActivite);
                }
                return [
                    {
                        titre: self.titreSectionValides() + self.suffixeSecteur(labelSecteurActivite),
                        candidats: candidatsValides
                    }, {
                        titre: titreSectionInteresses,
                        candidats: candidatsInteresses
                    },
                ]
            } else {
                return [{
                    titre: self.titreSectionValides(),
                    candidats: self.resultatRecherche.candidats
                }]
            }
        },
        metiersRecherchesParSecteurs: function() {
            var metiersFiltres = [];

            if (this.metierChoisi) {
                metiersFiltres = app.candidatCourant.metiersRecherches.filter(function(metier) {
                    return app.metierChoisi === metier.codeROME;
                });
            } else if (this.secteurActiviteChoisi) {
                metiersFiltres = app.candidatCourant.metiersRecherches.filter(function(metier) {
                    return app.secteurActiviteChoisi === metier.codeROME.charAt(0);
                });
            } else {
                metiersFiltres = app.candidatCourant.metiersRecherches;
            }

            var metiersParSecteur = app.metiersParSecteur(metiersFiltres);
            var result = [];
            for (var codeSecteur in metiersParSecteur) {
                result.push({
                    code: codeSecteur,
                    label: app.secteurActiviteParCode(codeSecteur).label,
                    metiers: metiersParSecteur[codeSecteur]
                });
            }
            return result;
        },
        pagesSavoirFaire: function() {
            var nbPages = Math.ceil(this.candidatCourant.savoirFaire.length / this.nbSavoirFaireParPage);
            var result = [];
            for (var i = 0; i < nbPages; i++) {
                result.push(i * this.nbSavoirFaireParPage);
            }
            return result;
        }
    },
    methods: {
        secteurActiviteParCode: function(codeSecteur) {
            return this.secteursActivites.find(function(s) {
                return s.code === codeSecteur;
            });
        },
        metiersParSecteur: function(metiers) {
            return metiers.reduce(function (acc, metier) {
                var key = metier.codeROME.charAt(0);
                acc[key] = acc[key] || [];
                acc[key].push(metier);
                return acc;
            }, {});
        },
        titreSansCandidatsValides: function() {
          return 'Nous n\'avons pour l\'instant aucun candidat validé MRS ' + this.imageValideMRS() + ', mais d\'autres candidats recherchent ';
        },
        titreSectionValides: function() {
            return 'Candidats Perspectives validés MRS ' + this.imageValideMRS();
        },
        suffixeSecteur: function(secteurActivite) {
            return ' dans le secteur "' + secteurActivite + '"';
        },
        suffixeMetier: function(metier) {
            return ' dans le domaine "' + metier + '"';
        },
        suffixeVille: function() {
            return (this.localisationChoisie) ? ' à "' + this.localisationChoisie + '"' : '';
        },
        imageValideMRS: function() {
            return '<img width="20" height="20" alt="MRS" src="/assets/images/commun/metier-valide.svg" />';
        },
        mrsMiseEnAvant: function(metierValide, metiersValidesRecherches) {
            if (this.metierChoisi) {
                return metierValide.indexOf(this.metierChoisi) !== -1 && metiersValidesRecherches.findIndex(function(metier) {
                    return metierValide.indexOf(metier.codeROME) !== -1;
                }) !== -1;
            } else if (this.secteurActiviteChoisi) {
                return metierValide.charAt(0) === this.secteurActiviteChoisi && metiersValidesRecherches.findIndex(function(metier) {
                    return metier.codeROME.charAt(0) === app.secteurActiviteChoisi;
                }) !== -1;
            }
            return false;
        },
        secteurMisEnAvant: function() {
            return (this.metierChoisi || this.secteurActiviteChoisi);
        },
        interessePar: function(metiers) { // FIXME : indexsection == 0 à nettoyer
            if (this.metierChoisi) {
                return metiers.filter(function(metier) {
                    return app.metierChoisi === metier.codeROME;
                });
            } else if (this.secteurActiviteChoisi) {
                return metiers.filter(function(metier) {
                    return app.secteurActiviteChoisi === metier.codeROME.charAt(0);
                });
            } else {
                return [];
            }
        },
        rechercherCandidatsSansPagination: function (e) {
            e.preventDefault();
            this.rechercherCandidats(null).done(function (response) {
                app.resultatRecherche.pages = response.pages;
                app.$refs.pagination.pageChargee(1);
            });
        },
        rechercherCandidats: function (filtrePagination) {
            var formData = [
                {name: "csrfToken", value: this.csrfToken},
                {name: "secteurActivite", value: this.secteurActivite},
                {name: "metier", value: this.metier}
            ];
            if (this.localisation) {
                formData.push({name: "coordonnees.latitude", value: this.localisation.latitude});
                formData.push({name: "coordonnees.longitude", value: this.localisation.longitude});
            }
            if (filtrePagination) {
                formData.push({name: "pagination.score", value: filtrePagination.score});
                formData.push({name: "pagination.dateInscription", value: filtrePagination.dateInscription});
                formData.push({name: "pagination.candidatId", value: filtrePagination.candidatId});
            }
            app.candidatCourant = null;

            return $.ajax({
                type: 'POST',
                url: '/recruteur/recherche',
                data: formData,
                dataType: 'json',
                beforeSend: function (xhr) {
                    app.display.chargement = true;
                }
            }).done(function (response) {
                app.secteurActiviteChoisi = app.secteurActivite;
                app.metierChoisi = app.metier;
                app.localisationChoisie = app.localisation.label;
                app.resultatRecherche.candidats = response.candidats;
                app.resultatRecherche.nbCandidats = response.nbCandidats;
                app.resultatRecherche.nbCandidatsTotal = response.nbCandidatsTotal;
            }).always(function () {
                app.display.chargement = false;
            });
        },
        onSecteurActiviteModifie: function () {
            this.metier = '';
        },
        doitAfficherCandidatPrecedent: function () {
            return !this.display.chargement && this.indexNavigationCandidat > 0;
        },
        doitAfficherCandidatSuivant: function () {
            return !this.display.chargement &&
                this.indexNavigationCandidat < (this.resultatRecherche.nbCandidatsTotal - 1);
        },
        chargerPage: function (index) {
            // on récupère le critere correspondant à la page et on la charge
            var filtrePagination = this.resultatRecherche.pages[index - 1];

            return this.rechercherCandidats(filtrePagination).done(function (response) {
                if (index === app.resultatRecherche.pages.length) {
                    if (response.nbCandidats === app.nbCandidatsParPage) {
                        app.resultatRecherche.pages.push(response.pageSuivante);
                    }
                }
                app.$refs.pagination.pageChargee(index);
            });
        },
        afficherCandidatSuivant: function () {
            var index = this.indexNavigationCandidat + 1;
            if (index !== 0 && index % this.nbCandidatsParPage === 0) {
                var pageACharger = this.$refs.pagination.getPageCourante() + 1;
                this.chargerPage(pageACharger).done(function (response) {
                    app.afficherCandidat(0);
                });
            } else {
                this.afficherCandidat(index % this.nbCandidatsParPage);
            }
        },
        afficherCandidatPrecedent: function () {
            var index = this.indexNavigationCandidat - 1;
            if (this.indexNavigationCandidat !== 0 &&
                this.indexNavigationCandidat % this.nbCandidatsParPage === 0) {
                var pageACharger = this.$refs.pagination.getPageCourante() - 1;
                this.chargerPage(pageACharger).done(function (response) {
                    app.afficherCandidat(app.resultatRecherche.candidats.length - 1);
                });
            } else {
                this.afficherCandidat(index % this.nbCandidatsParPage);
            }
        },
        afficherCandidat: function (index) {
            if (this.resultatRecherche.candidats[index]) {
                this.indexNavigationCandidat = index + ((this.$refs.pagination.getPageCourante() - 1) * this.nbCandidatsParPage);
                this.indexPaginationSavoirFaireCandidat = 1;
                this.candidatCourant = this.resultatRecherche.candidats[index];

                var habiletesParMetier = {};
                this.candidatCourant.metiersValides.forEach(function (m) {
                    habiletesParMetier[m.metier.codeROME] = false;
                });
                this.display.habiletesParMetier = Object.assign({}, habiletesParMetier);

                var ongletsDetailCandidat = [{courant: true, label: 'Son potentiel', ref: 'potentiel', scrollInterval: []}];
                if (this.candidatCourant.savoirEtre.length > 0 || this.candidatCourant.savoirFaire.length > 0) {
                    ongletsDetailCandidat.push({courant: false, label: 'Son profil', ref: 'profil', scrollInterval: []});
                }
                if (this.candidatCourant.experiencesProfessionnelles.length > 0) {
                    ongletsDetailCandidat.push({courant: false, label: 'Son expérience', ref: 'experiences', scrollInterval: []});
                }
                this.display.ongletsDetailCandidat = Object.assign([], ongletsDetailCandidat);
                this.$refs.modaleDetailCandidat.scrollTop = 0;

                this.display.contact = false;
                $('#detailCandidat').modal('show');
            }
        },
        cssTempsTravail: function (tempsTravail) {
            return (tempsTravail && listeTempsTravail[tempsTravail]) ? 'tempsTravail--' + tempsTravail : '';
        },
        afficherLangues: function() {
            return this.candidatCourant.langues.map(function(l) {
                return l.label + (niveauxLangues[l.niveau] ? ' ' + niveauxLangues[l.niveau].label : '') ;
            }).join(', ');
        },
        copierNumeroTelephone: function () {
            this.$refs.numeroTelephone.select();
            document.execCommand("copy");

            this.display.numeroTelephoneCopie = true;
            setTimeout(function () {
                app.display.numeroTelephoneCopie = false;
            }, 1000);
        },
        onScroll: function(e) {
            var scrollTop = this.$refs.modaleDetailCandidat.scrollTop;

            if (this.display.ongletsDetailCandidat.length > 1) {
                if (this.display.ongletsDetailCandidat[0].scrollInterval.length === 0) {
                   app.calculHauteurOnglets();
                }

                for (var onglet = 0; onglet < this.display.ongletsDetailCandidat.length; onglet++) {
                    if (scrollTop >= this.display.ongletsDetailCandidat[onglet].scrollInterval[0] &&
                        scrollTop < this.display.ongletsDetailCandidat[onglet].scrollInterval[1]) {
                        app.display.ongletsDetailCandidat[onglet].courant = true;
                    } else {
                        app.display.ongletsDetailCandidat[onglet].courant = false;
                    }
                }
            }
        },
        setOngletCourant: function(index) {
            if (this.display.ongletsDetailCandidat[0].scrollInterval.length === 0) {
                app.calculHauteurOnglets();
            }

            this.$refs.modaleDetailCandidat.scrollTop = this.display.ongletsDetailCandidat[index].scrollInterval[0];
        },
        calculHauteurOnglets: function() {
            var lastHeight = 0, sumHeight = 0;
            for (var onglet = 0; onglet < this.display.ongletsDetailCandidat.length; onglet++) {
                lastHeight = onglet === 0 ? 0 : this.display.ongletsDetailCandidat[onglet - 1].scrollInterval[1];
                sumHeight += this.$refs[this.display.ongletsDetailCandidat[onglet].ref].clientHeight;

                if (onglet === this.display.ongletsDetailCandidat.length - 1) {
                    this.display.ongletsDetailCandidat[onglet].scrollInterval = [lastHeight, this.$refs.modaleDetailCandidat.scrollHeight];
                } else {
                    this.display.ongletsDetailCandidat[onglet].scrollInterval = [lastHeight, sumHeight];
                }
            }
        },
        doitAfficherDernierBloc: function() {
            return this.candidatCourant &&
                (this.candidatCourant.formations.length > 0 ||
                    this.candidatCourant.langues.length > 0 ||
                    this.candidatCourant.centresInteret.length > 0 ||
                    this.candidatCourant.permis.length > 0);
        },
        doitAfficherSavoirFaire: function (index) {
            var max = this.indexPaginationSavoirFaireCandidat * this.nbSavoirFaireParPage;
            return index >= (max - this.nbSavoirFaireParPage) && index < (max);
        },
        chargerPageSavoirFaire(index) {
            this.indexPaginationSavoirFaireCandidat = index;
        }
    }
});