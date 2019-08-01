import Vue from 'vue';
import places from 'places.js';
import Temoignages from '../../composants/Temoignages.vue';
import rayonsRechercheOffres from "../../domain/offre/rayonRecherche";

new Vue({
    el: '#landingCandidat',
    components: {
        'temoignages': Temoignages
    },
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
                    texte: "Je viens de décrocher un poste aux Sables d'Olonne en tant qu'aide à domicile. Je vous remercie pour votre investissement !"
                }
            ],
            rechercheOffresFormData: {
                lieuTravail: null,
                codePostal: null,
                rayonRecherche: 0,
            },
            rechercheOffresFormErrors: [],
            rayonsRechercheOffres: rayonsRechercheOffres,
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
    },
    methods: {
        algoliaPlacesChange: function(suggestion) {
            this.rechercheOffresFormData.codePostal = suggestion.postcode;
            this.rechercheOffresFormData.lieuTravail = suggestion.name;
        },
        algoliaPlacesClear: function() {
            this.rechercheOffresFormData.codePostal = null;
            this.rechercheOffresFormData.lieuTravail = null;
        },
        rechercherOffres: function() {
            this.rechercheOffresFormErrors = [];
            if (this.rechercheOffresFormData.codePostal === null || this.rechercheOffresFormData.codePostal === '') {
                this.rechercheOffresFormErrors.push({champ: 'codePostal', label: 'Dites-nous où vous recherchez un emploi'});
            }

            if (this.rechercheOffresFormErrors.length === 0) {
                var params = [];
                params.push('codePostal=' + this.rechercheOffresFormData.codePostal);
                params.push('lieuTravail=' + this.rechercheOffresFormData.lieuTravail);
                params.push('rayonRecherche=' + this.rechercheOffresFormData.rayonRecherche);
                var uri = encodeURI(params.reduce(function(acc, param, index) {
                    return acc + (index === 0 ? '?' : '&') + param;
                }, '/candidat/offres'));
                window.location.assign(uri);
            }
        }
    }
});