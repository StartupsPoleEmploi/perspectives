import Vue from "vue";
import $ from "jquery";
import places from "places.js";
import 'bootstrap/js/dist/modal';
import '../../commun/filters.js';
import Pagination from '../../composants/Pagination.vue';
import rayonsRecherche from '../../domain/commun/rayonRecherche.js';
import typesContrats from '../../domain/offre/typesContrats.js';

var app = new Vue({
    el: '#rechercheOffres',
    components: {
        'pagination': Pagination
    },
    data: function () {
        return {
            isCandidatAuthentifie: jsData.candidatAuthentifie,
            cv: jsData.cv,
            csrfToken: jsData.csrfToken,
            nbOffresParPage: 10,
            offres: [],
            nbOffresTotal: 0,
            indexPaginationOffre: 1,
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
                metiersValides: []
            },
            rayonsRecherche: rayonsRecherche,
            typesContrats: typesContrats,
            secteursActivites: [],
            metiersValides: Object.assign([], jsData.metiersValides),
            algoliaPlacesConfig: jsData.algoliaPlacesConfig,
            display: {
                contact: false,
                chargement: false,
                modaleDetailOffre: false
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
            countries: ['fr'],
            templates: {
                value: function(suggestion) {
                    return suggestion.name;
                }
            }
        });
        placesAutocomplete.on('change', function(e) {
            self.algoliaPlacesChange(e.suggestion);
        });
        placesAutocomplete.on('clear', function() {
            self.algoliaPlacesClear();
        });

        window.location = '#';
        var modaleDetail = $('#detailOffre');
        modaleDetail.on('show.bs.modal', function () {
            self.display.modaleDetailOffre = true;
            window.location = '#detailOffre';
        }).on('hide.bs.modal', function () {
            self.display.modaleDetailOffre = false;
            window.location = '#';
        });
        window.onpopstate = function (event) {
            if (self.display.modaleDetailOffre && window.location.href.endsWith('#')) {
                modaleDetail.modal('hide');
            }
            if (!self.display.modaleDetailOffre &&  window.location.href.endsWith('#detailOffre')) {
                modaleDetail.modal('show');
            }
        };

        this.rechercherOffres();
    },
    computed: {
        pages: function () {
            var nbPages = Math.ceil(this.offres.length / this.nbOffresParPage);
            var result = [];
            for (var i = 0; i < nbPages; i++) {
                result.push(i * this.nbOffresParPage);
            }
            return result;
        },
        afficherNbResultats: function() {
            return this.nbOffresTotal > 0 && !this.display.chargement;
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
        chargerPage: function(index) {
            this.indexPaginationOffre = index;
            this.$refs.pagination.pageChargee(index);
        },
        cssTypeContrat: function (offre) {
            return (offre && offre.contrat && typesContrats[offre.contrat.code]) ? 'typeContrat--' + offre.contrat.code : 'typeContrat--default';
        },
        doitAfficherOffre: function (index) {
            var max = this.indexPaginationOffre * this.nbOffresParPage;
            return index >= (max - this.nbOffresParPage) && index < (max);
        },
        doitAfficherOffreSuivante: function() {
            return this.indexNavigationOffre !== (this.offres.length - 1);
        },
        doitAfficherOffrePrecedente: function() {
            return this.indexNavigationOffre !== 0;
        },
        afficherOffre: function (offre, index) {
            this.display.contact = false;
            if (offre.id !== this.offreCourante.id) {
                this.offreCourante = offre;
                this.indexNavigationOffre = index;

                $('#detailOffre').modal('show');
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
        doitAfficherMiseEnAvantInscription: function() {
            return !this.isCandidatAuthentifie;
        },
        doitAfficherMiseEnAvantCV: function() {
            return this.isCandidatAuthentifie && !this.cv;
        },
        doitAfficherCoordonnees1: function(contact) {
            return contact.coordonnees1 &&
                contact.coordonnees1 !== contact.urlPostuler &&
                contact.coordonnees1.indexOf(contact.email) === -1 &&
                contact.coordonnees1.indexOf(contact.telephone) === -1;
        },
        doitAfficherLienOrigineOffre: function(contact) {
            return !contact.email &&
                !contact.telephone &&
                !contact.urlPostuler &&
                !contact.coordonnees1;
        },
        afficherFiltres: function () {
            if ($(".formulaireRecherche-jsResponsive").is(":visible")) {
                $(".formulaireRecherche-conteneurFiltres").show();
                $(".formulaireRecherche-retourListeResultats").show();
            }
        },
        cacherFiltres: function () {
            if ($(".formulaireRecherche-jsResponsive").is(":visible")) {
                $(".formulaireRecherche-conteneurFiltres").hide();
                $(".formulaireRecherche-retourListeResultats").hide();
            }
        },
        rechercherOffres: function() {
            var self = this;
            $.ajax({
                type: "POST",
                url: "/candidat/offres",
                data: $("#js-rechercheOffresForm").serializeArray(),
                dataType: "json",
                beforeSend: function(xhr) {
                    self.display.chargement = true;
                }
            }).done(function (response) {
                self.offres = response.offres;
                self.nbOffresTotal = response.nbOffresTotal;
                self.indexPaginationOffre = 1;
                self.$refs.pagination.pageChargee(1);
                self.cacherFiltres();
            }).always(function () {
                self.display.chargement = false;
            });
        }
    }
});