"use strict";

import filters from '../../commun/filters.js';
import pagination from '../../composants/pagination.js';

var app = new Vue({
    el: '#listeOffres',
    data: function () {
        return {
            candidat: jsData.candidat,
            secteursActivites: jsData.secteursActivites,
            nbOffresParPage: 10,
            offres: jsData.offres,
            indexCourant: 0,
            offreCourante: {},
            display: {
                contact: false,
                criteres: false
            },
            metiersRecherchesParSecteur: [],
            metiersEvaluesParSecteur: []
        }
    },
    beforeMount: function () {
        this.metiersEvaluesParSecteur = this.metiersParSecteur(this.candidat.metiersEvalues);
        this.metiersRecherchesParSecteur = this.metiersParSecteur(this.candidat.metiersRecherches);
    },
    computed: {
        pagesInitiales: function () {
            return this.calculerPages();
        }
    },
    methods: {
        metiersParSecteur: function (metiers) {
            var self = this;
            var result = [];
            metiers.forEach(function (m) {
                var indexSecteur = result.findIndex(function (s) {
                    return s.code === m.codeROME.charAt(0);
                });
                if (indexSecteur !== -1) {
                    result[indexSecteur].metiers.push(m);
                } else {
                    var secteurActivite = self.secteursActivites.find(function(s) {return s.code === m.codeROME.charAt(0)});
                    result.push({
                        code: secteurActivite.code,
                        label: secteurActivite.label,
                        metiers: [m]
                    });
                }
            });
            return result;
        },
        labelSecteurActivite: function (codeSecteurActivite) {
            return this.secteursActivites.find(function (s) {
                return s.code === codeSecteurActivite;
            }).label;
        },
        chargerPageSuivante: function (critere) {
            this.indexCourant = critere;
            app.$refs.pagination.pageSuivanteChargee(0, null); // pas de page suivante
        },
        chargerPagePrecedente: function (critere, index) {
            this.indexCourant = critere;
            app.$refs.pagination.pagePrecedenteChargee(index);
        },
        calculerPages: function () {
            var nbPages = Math.ceil(this.offres.length / this.nbOffresParPage);
            var result = [];
            for (var i = 0; i < nbPages; i++) {
                result.push(i * this.nbOffresParPage);
            }
            return result;
        },
        afficherOffre: function (index) {
            return index >= this.indexCourant && index < (this.indexCourant + this.nbOffresParPage);
        },
        toggleOffreCourante: function (offre) {
            this.display.contact = false;
            if (offre.id !== this.offreCourante) {
                this.offreCourante = offre;

                $('#js-modaleDetailOffre').modal('show');
            } else {
                this.offreCourante = null;
            }
        }
    }
});