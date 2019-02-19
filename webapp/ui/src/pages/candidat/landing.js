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
                rayonRecherche: null,
            },
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
        rechercherOffres: function() {
            var params = [];
            if (this.recherche.codePostal !== null && this.recherche.codePostal !== '') {
                params.push('codePostal=' + this.recherche.codePostal);
            }
            if (this.recherche.lieuTravail !== null && this.recherche.lieuTravail !== '') {
                params.push('lieuTravail=' + this.recherche.lieuTravail);
            }
            if (this.recherche.rayonRecherche !== null && this.recherche.rayonRecherche !== '') {
                params.push('rayonRecherche=' + this.recherche.rayonRecherche);
            }
            if (params.length > 0) {
                var uri = encodeURI(params.reduce(function(acc, param, index) {
                    return acc + (index === 0 ? '?' : '&') + param;
                }, '/candidat/offres'));
                window.location.assign(uri);
            }
        }
    }
});