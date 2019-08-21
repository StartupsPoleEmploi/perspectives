import Vue from 'vue';
import $ from 'jquery';
import 'bootstrap/js/dist/modal';
import '../../commun/filters.js';
import Pagination from '../../composants/Pagination.vue';
import Places from '../../composants/Places.vue';
import rayonsRechercheOffre from "../../domain/offre/rayonRecherche";
import typesContrats from '../../domain/offre/typesContrats.js';

new Vue({
    el: '#rechercheOffres',
    components: {
        'pagination': Pagination,
        'places': Places
    },
    data: function () {
        return {
            isCandidatAuthentifie: jsData.candidatAuthentifie,
            cv: jsData.cv,
            csrfToken: jsData.csrfToken,
            nbOffresParPage: 10,
            offres: [],
            pageSuivante: null,
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
            rechercheFormData: Object.assign({
                motCle: null,
                localisation: {
                    codePostal: null,
                    lieuTravail: null,
                    rayonRecherche: 0
                },
                typesContrats: [],
                metiers: []
            }, jsData.rechercheFormData),
            rechercheFormErrors: {},
            rayonsRecherche: rayonsRechercheOffre,
            typesContrats: typesContrats,
            metiersValides: Object.assign([], jsData.metiersValides),
            placesOptions : {
                appId: jsData.algoliaPlacesConfig.appId,
                apiKey: jsData.algoliaPlacesConfig.apiKey
            },
            display: {
                contact: false,
                chargement: false,
                erreurRecherche: false,
                modaleDetailOffre: false
            }
        }
    },
    created: function() {
        if (!this.rechercheFormData.localisation.rayonRecherche) {
            this.rechercheFormData.localisation.rayonRecherche = 0;
        }
    },
    mounted: function () {
        var self = this;
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
            if (!self.display.modaleDetailOffre && window.location.href.endsWith('#detailOffre')) {
                modaleDetail.modal('show');
            }
        };

        this.rechercherOffresSansPagination();
    },
    computed: {
        pages: function () {
            var nbPages = Math.ceil(this.offres.length / this.nbOffresParPage);
            var result = [];
            for (var i = 0; i < nbPages; i++) {
                result.push(i * this.nbOffresParPage);
            }
            return result;
        }
    },
    methods: {
        placesChange: function (suggestion) {
            this.rechercheFormData.localisation.codePostal = suggestion.postcode;
            this.rechercheFormData.localisation.lieuTravail = suggestion.name;

            var localisation = this.rechercheFormData.localisation;
            history.pushState({}, '', window.location.pathname + '?codePostal=' + localisation.codePostal + '&lieuTravail=' + localisation.lieuTravail + '&rayonRecherche=' + localisation.rayonRecherche + '#');
        },
        placesClear: function () {
            this.rechercheFormData.localisation.codePostal = null;
            this.rechercheFormData.localisation.lieuTravail = null;
        },
        chargerPage: function (index) {
            if (index === this.pages.length && this.pageSuivante) {
                var self = this;
                this.rechercherOffres(index, this.pageSuivante).done(function (response) {
                    self.offres = self.offres.concat(response.offres);
                });
            } else {
                this.indexPaginationOffre = index;
                this.$refs.pagination.pageChargee(index);
            }
        },
        cssTypeContrat: function (offre) {
            return (offre && offre.contrat && typesContrats[offre.contrat.code]) ? 'typeContrat--' + offre.contrat.code : 'typeContrat--default';
        },
        doitAfficherOffre: function (index) {
            var max = this.indexPaginationOffre * this.nbOffresParPage;
            return index >= (max - this.nbOffresParPage) && index < (max);
        },
        doitAfficherOffreSuivante: function () {
            return this.indexNavigationOffre !== (this.offres.length - 1) || this.pageSuivante;
        },
        doitAfficherOffrePrecedente: function () {
            return this.indexNavigationOffre !== 0;
        },
        afficherOffre: function (offre, index) {
            this.display.contact = false;
            this.offreCourante = offre;
            this.indexNavigationOffre = index;

            $('#detailOffre').modal('show');
        },
        afficherOffreSuivante: function () {
            this.display.contact = false;
            this.indexNavigationOffre = this.indexNavigationOffre + 1;

            if (this.indexNavigationOffre === this.offres.length && this.pageSuivante) {
                var self = this;
                this.rechercherOffres(this.pages.length + 1, this.pageSuivante).done(function (response) {
                    self.offres = self.offres.concat(response.offres);
                    self.offreCourante = self.offres[self.indexNavigationOffre];
                });
            } else {
                this.offreCourante = this.offres[this.indexNavigationOffre];
            }
        },
        afficherOffrePrecedente: function () {
            this.display.contact = false;
            this.indexNavigationOffre = this.indexNavigationOffre - 1;
            this.offreCourante = this.offres[this.indexNavigationOffre];
        },
        doitAfficherMiseEnAvantInscription: function () {
            return !this.isCandidatAuthentifie;
        },
        doitAfficherMiseEnAvantCV: function () {
            return this.isCandidatAuthentifie && !this.cv;
        },
        doitAfficherCoordonnees1: function (contact) {
            return contact.coordonnees1 &&
                contact.coordonnees1 !== contact.urlPostuler &&
                contact.coordonnees1.indexOf(contact.email) === -1 &&
                contact.coordonnees1.indexOf(contact.telephone) === -1;
        },
        doitAfficherLienOrigineOffre: function (contact) {
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
        rechercherOffresSansPagination: function () {
            var self = this;
            this.rechercherOffres(1, null).done(function (response) {
                self.offres = response.offres;
            });
        },
        rechercherOffres: function(index, pageSuivante) {
            var self = this;
            var formData = $("#js-rechercheOffresForm").serializeArray();
            if (pageSuivante) {
                formData.push({name: "page.debut", value: pageSuivante.debut});
                formData.push({name: "page.fin", value: pageSuivante.fin});
            }

            return $.ajax({
                type: "POST",
                url: "/candidat/offres",
                data: formData,
                dataType: "json",
                beforeSend: function () {
                    self.display.chargement = true;
                }
            }).done(function (response) {
                self.indexPaginationOffre = index;
                self.$refs.pagination.pageChargee(index);
                self.pageSuivante = response.pageSuivante;
                self.cacherFiltres();
                self.display.erreurRecherche = false;
                self.rechercheFormErrors = {};
            }).fail(function (jqXHR) {
                if (jqXHR.status === 400) {
                    self.rechercheFormErrors = jqXHR.responseJSON;
                } else {
                    self.rechercheFormErrors = {};
                    self.display.erreurRecherche = true;
                }
            }).always(function () {
                self.display.chargement = false;
            });
        }
    }
});