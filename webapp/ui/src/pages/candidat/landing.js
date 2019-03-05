import Vue from 'vue';
import places from 'places.js';
import '../../composants/temoignages.js';
import rayonsRecherche from "../../domain/commun/rayonRecherche";

var app = new Vue({
    el: '#landingCandidat',
    data: function () {
        return {
            temoignages: [
                {
                    source: "Rebecca S.",
                    texte: "Le lendemain de mon inscription, j'ai été contactée par un employeur ainsi qu'une agence d’intérim. On m'a déjà proposé un essai !"
                },
                {
                    source: "Géraldine R.",
                    texte: "Je reprends un emploi cet été en espérant plus pour la suite. Je suis très contente. Merci."
                },
                {
                    source: "Jessica H.",
                    texte: "Je viens de décrocher un poste aux Sables d'olonne en tant qu'aide à domicile. Je vous remercie pour votre investissement !"
                }
            ],
            recherche: {
                lieuTravail: null,
                codePostal: null,
                rayonRecherche: 10,
            },
            rechercheOffresFormErrors: [],
            rayonsRecherche: rayonsRecherche,
            algoliaPlacesConfig: jsData.algoliaPlacesConfig
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
    methods: {
        algoliaPlacesChange: function(suggestion) {
            this.recherche.codePostal = suggestion.postcode;
            this.recherche.lieuTravail = suggestion.name;
        },
        algoliaPlacesClear: function() {
            this.recherche.codePostal = null;
            this.recherche.lieuTravail = null;
        },
        hasError: function(champ) {
            return this.rechercheOffresFormErrors.findIndex(function(element) {
                return element.champ === champ;
            }) !== -1;
        },
        rechercherOffres: function() {
            this.rechercheOffresFormErrors = [];
            if (this.recherche.codePostal === null || this.recherche.codePostal === '') {
                this.rechercheOffresFormErrors.push({champ: 'codePostal', label: 'Dites-nous où vous recherchez un job'});
            }
            if (this.recherche.rayonRecherche === null || this.recherche.rayonRecherche === '') {
                this.rechercheOffresFormErrors.push({champ: 'rayonRecherche', label: 'Renseignez un rayon de recherche'});
            }

            if (this.rechercheOffresFormErrors.length === 0) {
                var params = [];
                params.push('codePostal=' + this.recherche.codePostal);
                params.push('lieuTravail=' + this.recherche.lieuTravail);
                params.push('rayonRecherche=' + this.recherche.rayonRecherche);
                var uri = encodeURI(params.reduce(function(acc, param, index) {
                    return acc + (index === 0 ? '?' : '&') + param;
                }, '/candidat/offres'));
                window.location.assign(uri);
            }
        }
    }
});