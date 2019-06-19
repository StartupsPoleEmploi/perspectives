import Vue from "vue";
import $ from "jquery";
import places from "places.js";
import 'bootstrap/js/dist/modal';
import '../../commun/filters.js';
import '../../composants/pagination.js';
import rayonsRecherche from '../../domain/commun/rayonRecherche.js';
import typesContrats from '../../domain/offre/typesContrats.js';

var app = new Vue({
    el: '#rechercheOffres',
    data: function () {
        return {
            isCandidatAuthentifie: jsData.candidatAuthentifie,
            cv: jsData.cv,
            csrfToken: jsData.csrfToken,
            nbOffresParPage: 10,
            offres: jsData.offres,
            nbOffresTotal: jsData.nbOffresTotal,
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
                metiersValides: []
            },
            rayonsRecherche: rayonsRecherche,
            typesContrats: typesContrats,
            secteursActivites: [],
            metiersValides: Object.assign([], jsData.metiersValides),
            algoliaPlacesConfig: jsData.algoliaPlacesConfig,
            display: {
                contact: false,
                offreSuivante: this.offreCourante != null,
                offrePrecedente: this.offreCourante != null,
                chargement: false,
                nbResultats: false,
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
    },
    computed: {
        pagesInitiales: function () {
            return this.calculerPages();
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
                dataType: "json",
                beforeSend: function(xhr) {
                    app.display.chargement = true;
                }
            }).done(function (response) {
                app.offres = response.offres;
                app.nbOffresTotal = response.nbOffresTotal;
                app.$refs.pagination.modifierPagination(app.calculerPages());
                app.indexPaginationOffre = 0;
                app.cacherFiltres();
            }).fail(function () {
            }).always(function () {
                app.display.chargement = false;
            });
        }
    }
});