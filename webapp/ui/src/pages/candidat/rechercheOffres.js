import rayonsRecherche from '../../domain/commun/rayonRecherche.js';
import secteursActivites from '../../domain/commun/secteurActivite.js';
import typesContrats from '../../domain/offre/typeContrat.js';
import filters from '../../commun/filters.js';
import pagination from '../../composants/pagination.js';
import places from "places.js";

var app = new Vue({
    el: '#rechercheOffres',
    data: function () {
        return {
            isCandidatAuthentifie: jsData.candidatAuthentifie,
            csrfToken: jsData.csrfToken,
            nbOffresParPage: 10,
            offres: jsData.offres,
            indexCourant: 0,
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
                secteursActivites: []
            },
            rayonsRecherche: rayonsRecherche,
            typesContrats: typesContrats,
            secteursActivites: secteursActivites,
            algoliaPlacesConfig: jsData.algoliaPlacesConfig,
            display: {
                contact: false,
                offreSuivante: this.offreCourante != null && false,
                offrePrecedente: this.offreCourante != null &&  false,
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
        formations: function(offre) {
            return offre.formations.reduce(function(acc, f, index) {
                var label = app.labelFormation(f);
                if (label !== null) {
                    return acc + (index === 0 ? '' : ', ') + label;
                } else return acc;
            }, '');
        },
        labelFormation: function (formation) {
            if (formation.niveau !== undefined && formation.domaine !== undefined) {
                return formation.niveau + ' en ' + formation.domaine + ' ' + (formation.exige ? 'exigé' : 'souhaité');
            } else return null;
        },
        permis: function(offre) {
            return offre.permis.reduce(function(acc, p, index) {
                return acc + (index === 0 ? '' : ', ') + app.labelPermis(p);
            }, '');
        },
        labelPermis: function(permis) {
            return 'Permis ' + permis.label + ' ' + (permis.exige ? 'exigé' : 'souhaité');
        },
        langues: function(offre) {
            return offre.langues.reduce(function(acc, l, index) {
                return acc + (index === 0 ? '' : ', ') + app.labelLangue(l);
            }, '');
        },
        labelLangue: function(langue) {
            return langue.label + ' ' + (langue.exige ? 'exigé' : 'souhaité');
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
        doitAfficherIndex: function (index) {
            return index >= this.indexCourant && index < (this.indexCourant + this.nbOffresParPage);
        },
        doitAfficherOffreSuivante: function() {
            return this.indexCourant !== (this.offres.length - 1);
        },
        doitAfficherOffrePrecedente: function() {
            return this.indexCourant !== 0;
        },
        afficherOffre: function (offre, index) {
            this.display.contact = false;
            if (offre.id !== this.offreCourante) {
                this.offreCourante = offre;
                this.indexCourant = index;

                $('#js-modaleDetailOffre').modal('show');
            } else {
                this.offreCourante = null;
                this.indexCourant = null;
            }
        },
        afficherOffreSuivante: function() {
            this.indexCourant = this.indexCourant + 1;
            this.offreCourante = this.offres[this.indexCourant];
        },
        afficherOffrePrecedente: function() {
            this.indexCourant = this.indexCourant - 1;
            this.offreCourante = this.offres[this.indexCourant];
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
            }).fail(function () {
            });
        }
    }
});