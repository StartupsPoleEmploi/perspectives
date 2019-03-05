import Vue from "vue";
import $ from "jquery";
import places from "places.js";
import 'bootstrap/js/dist/modal';
import '../../commun/filters.js';
import '../../composants/pagination.js';
import rayonsRecherche from '../../domain/commun/rayonRecherche.js';
import secteursActivites from '../../domain/commun/secteurActivite.js';
import typesContrats from '../../domain/offre/typeContrat.js';

var app = new Vue({
    el: '#rechercheOffres',
    data: function () {
        return {
            isCandidatAuthentifie: jsData.candidatAuthentifie,
            cv: jsData.cv,
            csrfToken: jsData.csrfToken,
            nbOffresParPage: 10,
            offres: jsData.offres,
            indexPaginationOffre: 0,
            indexNavigationOffre: 0,
            offreCourante: {
                contrat: {},
                lieuTravail: {},
                salaire: {},
                entreprise: {},
                contact: {},
                formations: [],
                permis: [],
                langues: []
            },
            recherche: {
                motCle: jsData.recherche.motCle,
                lieuTravail: jsData.recherche.lieuTravail,
                codePostal: jsData.recherche.codePostal,
                rayonRecherche: jsData.recherche.rayonRecherche,
                typesContrats: [],
                secteursActivites: [],
                metiersEvalues: []
            },
            rayonsRecherche: rayonsRecherche,
            typesContrats: typesContrats,
            secteursActivites: secteursActivites,
            metiersEvalues: jsData.recherche.metiersEvalues,
            algoliaPlacesConfig: jsData.algoliaPlacesConfig,
            display: {
                contact: false,
                offreSuivante: this.offreCourante != null && false,
                offrePrecedente: this.offreCourante != null &&  false
            }
        }
    },
    mounted: function () {
        var self = this;
        var placesAutocomplete = places({
            appId: self.algoliaPlacesConfig.appId,
            apiKey: self.algoliaPlacesConfig.apiKey,
            container: document.querySelector('#js-lieuTravail'),
            type: 'city',
            aroundLatLngViaIP: false,
            style: false,
            useDeviceLocation: false,
            language: 'fr',
            countries: ['fr']
        });
        placesAutocomplete.on('change', function(e) {
            self.algoliaPlacesChange(e.suggestion);
        });
        placesAutocomplete.on('clear', function() {
            self.algoliaPlacesClear();
        });
    },
    computed: {
        pagesInitiales: function () {
            return this.calculerPages();
        }
    },
    methods: {
        algoliaPlacesChange: function(suggestion) {
            this.recherche.codePostal = suggestion.postcode;
            this.recherche.lieuTravail = suggestion.name;
        },
        algoliaPlacesClear: function() {
            this.recherche.codePostal = null;
            this.recherche.lieuTravail = null;
        },
        chargerPageSuivante: function (critere) {
            this.indexPaginationOffre = critere;
            app.$refs.pagination.pageSuivanteChargee(0, null); // pas de page suivante
        },
        chargerPagePrecedente: function (critere, index) {
            this.indexPaginationOffre = critere;
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
        cssTypeContrat: function (offre) {
            return (offre !== undefined &&
                offre.contrat !== undefined &&
                typesContrats[offre.contrat.code] !== undefined) ? 'typeContrat--' + offre.contrat.code : 'typeContrat--default';
        },
        doitAfficherOffre: function (index) {
            return index >= this.indexPaginationOffre && index < (this.indexPaginationOffre + this.nbOffresParPage);
        },
        doitAfficherOffreSuivante: function() {
            return this.indexNavigationOffre !== (this.offres.length - 1);
        },
        doitAfficherOffrePrecedente: function() {
            return this.indexNavigationOffre !== 0;
        },
        afficherOffre: function (offre, index) {
            this.display.contact = false;
            if (offre.id !== this.offreCourante) {
                this.offreCourante = offre;
                this.indexNavigationOffre = index;

                $('#js-modaleDetailOffre').modal('show');
            } else {
                this.offreCourante = null;
                this.indexNavigationOffre = null;
            }
        },
        afficherOffreSuivante: function() {
            this.display.contact = false;
            this.indexNavigationOffre = this.indexNavigationOffre + 1;
            this.offreCourante = this.offres[this.indexNavigationOffre];
        },
        afficherOffrePrecedente: function() {
            this.display.contact = false;
            this.indexNavigationOffre = this.indexNavigationOffre - 1;
            this.offreCourante = this.offres[this.indexNavigationOffre];
        },
        doitAfficherMiseEnAvantInscription: function(index) {
            return !this.isCandidatAuthentifie && this.indexPaginationOffre === 0 && index === 2;
        },
        doitAfficherMiseEnAvantCV: function(index) {
            return this.isCandidatAuthentifie && !this.cv && this.indexPaginationOffre === 0 && index === 3;
        },
        afficherFiltres: function () {
            if ($(".formulaireRecherche-jsResponsive").is(":visible")) {
                $(".formulaireRecherche-conteneurFiltres").show();
                $(".formulaireRecherche-retourListeResultats").show();
                $(".rechercheOffres-nbResultats").hide();
            }
        },
        cacherFiltres: function () {
            if ($(".formulaireRecherche-jsResponsive").is(":visible")) {
                $(".formulaireRecherche-conteneurFiltres").hide();
                $(".formulaireRecherche-retourListeResultats").hide();
                $(".rechercheOffres-nbResultats").show();
            }
        },
        rechercherOffres: function() {
            var formData = $("#js-rechercheOffresForm").serializeArray();
            formData.push({name: "csrfToken", value: this.csrfToken});
            $.ajax({
                type: "POST",
                url: "/candidat/offres",
                data: formData,
                dataType: "json"
            }).done(function (response) {
                app.offres = response.offres;
                app.$refs.pagination.modifierPagination(app.calculerPages());
                app.indexPaginationOffre = 0;
                app.cacherFiltres();
            }).fail(function () {
            });
        }
    }
});