"use strict";

import filters from '../../commun/filters.js';
import pagination from '../../composants/pagination.js';

var app = new Vue({
    el: '#rechercheOffres',
    data: function () {
        return {
            nbOffresParPage: 10,
            offres: jsData.offres,
            indexCourant: 0,
            offreCourante: {},
            display: {
                contact: false
            }
        }
    },
    computed: {
        pagesInitiales: function () {
            return this.calculerPages();
        }
    },
    methods: {
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