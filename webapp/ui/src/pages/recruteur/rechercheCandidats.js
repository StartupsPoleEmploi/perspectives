import Vue from 'vue';
import $ from 'jquery';
import 'bootstrap/js/dist/modal';
import Pagination from '../../composants/Pagination.vue';
import Places from '../../composants/Places.vue';
import '../../commun/filters.js';
import listeTempsTravail from '../../domain/candidat/tempsTravail';
import niveauxLangues from '../../domain/candidat/niveauxLangues';
import ROME from '../../domain/metier/ROME';
import tracking from '../../commun/tracking';

Vue.filter('dateExperience', function (value) {
    return new Date(value).toLocaleString('fr-FR', {month: 'long', year: "numeric"});
});

var app = new Vue({
    el: '#rechercheCandidats',
    components: {
        'pagination': Pagination,
        'places': Places
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
            resultatRecherche: {
                candidats: jsData.resultatRecherche.candidats,
                nbCandidatsTotal: jsData.resultatRecherche.nbCandidatsTotal,
                pages: []
            },
            placesOptions: {
                appId: jsData.algoliaPlacesConfig.appId,
                apiKey: jsData.algoliaPlacesConfig.apiKey,
                style: true
            },
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
    created: function() {
        tracking.trackCommonActions();
        this.buildPages(jsData.resultatRecherche.pagesSuivantes);
    },
    mounted: function () {
        var self = this;
        window.location = '#';
        var modaleDetail = $('#detailCandidat');
        modaleDetail.on('show.bs.modal', function () {
            self.display.modaleDetailCandidat = true;
            window.location = '#detailCandidat';
        }).on('hide.bs.modal', function () {
            self.display.modaleDetailCandidat = false;
            window.location = '#';
            tracking.sendEvent(tracking.Events.RECRUTEUR_FERMETURE_DETAIL_CANDIDAT, self.contexteCandidatCourant());
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
                        return self.secteurActiviteChoisi === ROME.codeSecteurActivite(m.codeROME);
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
                    return app.secteurActiviteChoisi === ROME.codeSecteurActivite(metier.codeROME);
                });
            } else {
                metiersFiltres = app.candidatCourant.metiersRecherches;
            }

            var metiersParSecteur = ROME.metiersParSecteur(metiersFiltres);
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
        placesChange: function (suggestion) {
            this.localisation = {
                label: suggestion.name,
                latitude: suggestion.latlng.lat,
                longitude: suggestion.latlng.lng
            };
        },
        placesClear: function () {
            this.localisation = {
                label: '',
                latitude: null,
                longitude: null
            };
        },
        secteurActiviteParCode: function(codeSecteur) {
            return this.secteursActivites.find(function(s) {
                return s.code === codeSecteur;
            });
        },
        codeSecteurActivite: function(codeROME) {
            return ROME.codeSecteurActivite(codeROME);
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
                return ROME.codeSecteurActivite(metierValide) === this.secteurActiviteChoisi && metiersValidesRecherches.findIndex(function(metier) {
                    return ROME.codeSecteurActivite(metier.codeROME) === app.secteurActiviteChoisi;
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
                    return app.secteurActiviteChoisi === ROME.codeSecteurActivite(metier.codeROME);
                });
            } else {
                return [];
            }
        },
        rechercherCandidatsSansPagination: function (e) {
            e.preventDefault();
            this.rechercherCandidats({}).done(function (response) {
                app.buildPages(response.pagesSuivantes);
                app.$refs.pagination.pageChargee(1);
            });
        },
        buildPages: function(pagesSuivantes) {
            // pas de filtre de pagination pour la première page
            this.resultatRecherche.pages = [{}].concat(pagesSuivantes);
        },
        chargerPage: function (index) {
            var filtrePagination = this.resultatRecherche.pages[index - 1];

            return this.rechercherCandidats(filtrePagination).done(function (response) {
                if (index === app.resultatRecherche.pages.length) {
                    app.resultatRecherche.pages = app.resultatRecherche.pages.concat(response.pagesSuivantes);
                }
                app.$refs.pagination.pageChargee(index);
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
            if (filtrePagination.dateInscription && filtrePagination.candidatId) {
                formData.push({name: "pagination.score", value: filtrePagination.score});
                formData.push({name: "pagination.dateInscription", value: filtrePagination.dateInscription});
                formData.push({name: "pagination.candidatId", value: filtrePagination.candidatId});
            }
            app.candidatCourant = null;

            tracking.sendEvent(tracking.Events.RECRUTEUR_RECHERCHE_CANDIDAT, {
                'secteur_activite': this.secteurActivite,
                'metiers': this.metier,
                'localisation': this.localisation ? this.localisation.label : ''
            });

            return $.ajax({
                type: 'POST',
                url: '/recruteur/recherche',
                data: formData,
                dataType: 'json',
                beforeSend: function () {
                    app.display.chargement = true;
                }
            }).done(function (response) {
                app.secteurActiviteChoisi = app.secteurActivite;
                app.metierChoisi = app.metier;
                app.localisationChoisie = app.localisation.label;
                app.resultatRecherche.candidats = response.candidats;
                app.resultatRecherche.nbCandidatsTotal = response.nbCandidatsTotal;

                tracking.sendEvent(tracking.Events.RECRUTEUR_AFFICHAGE_RESULTATS_RECHERCHE_CANDIDAT, {
                    'page_courante': app.$refs.pagination ? app.$refs.pagination.getPageCourante() : 1,
                    'nb_resultats': response.nbCandidatsTotal
                });
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
        afficherCandidatSuivant: function () {
            var index = this.indexNavigationCandidat + 1;
            if (index !== 0 && index % this.nbCandidatsParPage === 0) {
                var pageACharger = this.$refs.pagination.getPageCourante() + 1;
                this.chargerPage(pageACharger).done(function (response) {
                    app.afficherCandidat(0, 'clic_btn_suivant');
                });
            } else {
                this.afficherCandidat(index % this.nbCandidatsParPage,'clic_btn_suivant');
            }
        },
        afficherCandidatPrecedent: function () {
            var index = this.indexNavigationCandidat - 1;
            if (this.indexNavigationCandidat !== 0 &&
                this.indexNavigationCandidat % this.nbCandidatsParPage === 0) {
                var pageACharger = this.$refs.pagination.getPageCourante() - 1;
                this.chargerPage(pageACharger).done(function (response) {
                    app.afficherCandidat(app.resultatRecherche.candidats.length - 1, 'clic_btn_precedent');
                });
            } else {
                this.afficherCandidat(index % this.nbCandidatsParPage, 'clic_btn_precedent');
            }
        },
        afficherCandidat: function (index, source) {
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

                tracking.sendEvent(tracking.Events.RECRUTEUR_AFFICHAGE_DETAIL_CANDIDAT, Object.assign({
                    'source': source ? source : 'liste'
                }, this.contexteCandidatCourant()));

                $('#detailCandidat').modal('show');
            }
        },
        deplierOuReplierHabiletes: function(metierValide) {
            if(!this.display.habiletesParMetier[metierValide.metier.codeROME]) {
                // on ne tracke que si les habiletes ne sont pas deja depliees
                tracking.sendEvent(tracking.Events.RECRUTEUR_DETAIL_CANDIDAT_MRS, this.contexteCandidatCourant());
            }
            this.display.habiletesParMetier[metierValide.metier.codeROME] = !this.display.habiletesParMetier[metierValide.metier.codeROME];
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

                        let event = this.eventPourOngletCourant();
                        if (event) {
                            tracking.sendEvent(event, this.contexteCandidatCourant());
                        }

                    } else {
                        app.display.ongletsDetailCandidat[onglet].courant = false;
                    }
                }
            }
        },
        eventPourOngletCourant: function() {
            let event;
            let ongletAffiche = this.display.ongletsDetailCandidat.find(onglet => onglet.courant === true);
            if (ongletAffiche) {
                let labelAffiche = ongletAffiche.label;
                if (labelAffiche === 'Son potentiel') event = tracking.Events.RECRUTEUR_DETAIL_CANDIDAT_POTENTIEL;
                else if (labelAffiche === 'Son profil') event = tracking.Events.RECRUTEUR_DETAIL_CANDIDAT_PROFIL;
                else if (labelAffiche === 'Son expérience') event = tracking.Events.RECRUTEUR_DETAIL_CANDIDAT_EXPERIENCE;
            }
            return event;
        },
        contexteCandidatCourant: function() {
            return {
                'candidat_id': this.candidatCourant.candidatId,
                'metiers_valides': this.candidatCourant.metiersValides.map(x => x.metier.codeROME).join(', '),
                'code_postal': this.candidatCourant.codePostal,
                'localisation': this.candidatCourant.commune
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
        chargerPageSavoirFaire: function(index) {
            tracking.sendEvent(tracking.Events.CANDIDAT_AFFICHAGE_DETAIL_OFFRE, Object.assign({
                'page_courante': index
            }, this.contexteCandidatCourant()));
            this.indexPaginationSavoirFaireCandidat = index;
        }
    }
});
