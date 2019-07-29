import Vue from 'vue';
import places from 'places.js';

var app = new Vue({
    el: '#profilRecruteur',
    data: function () {
        return {
            profilFormData: Object.assign({
                nouveauRecruteur: false,
                typeRecruteur: null,
                raisonSociale: null,
                numeroSiret: null,
                adresse: {
                    codePostal: null,
                    commune: null,
                    pays: null
                },
                numeroTelephone: null,
                contactParCandidats: null
            }, jsData.profilFormData),
            profilFormErrors: Object.assign({
                typeRecruteur: [],
                raisonSociale: [],
                numeroSiret: [],
                adresse: [],
                numeroTelephone: [],
                contactParCandidats: []
            }, jsData.profilFormErrors),
            typesRecruteur: [
                {value: 'ENTREPRISE', label: 'Une entreprise'},
                {value: 'AGENCE_INTERIM', label: 'Une agence d\'interim'},
                {value: 'ORGANISME_FORMATION', label: 'Un organisme de formation'}
            ],
            algoliaPlacesConfig: jsData.algoliaPlacesConfig
        }
    },
    created: function() {
        // FIXME : Lien entre back et front : le back renvoit la key en string plutot qu'en json (fix à faire cote back)
        if (jsData.profilFormData.hasOwnProperty('adresse.codePostal')) {
            this.profilFormData.adresse.codePostal = jsData.profilFormData['adresse.codePostal'];
        }
        if (jsData.profilFormData.hasOwnProperty('adresse.commune')) {
            this.profilFormData.adresse.commune = jsData.profilFormData['adresse.commune'];
        }
        if (jsData.profilFormData.hasOwnProperty('adresse.pays')) {
            this.profilFormData.adresse.pays = jsData.profilFormData['adresse.pays'];
        }
    },
    mounted: function () {
        var self = this;
        var placesAutocomplete = places({
            appId: self.algoliaPlacesConfig.appId,
            apiKey: self.algoliaPlacesConfig.apiKey,
            container: document.querySelector('#js-adresse'),
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
        placesAutocomplete.on('change', function (e) {
            self.algoliaPlacesChange(e.suggestion);
        });
        placesAutocomplete.on('clear', function () {
            self.algoliaPlacesClear();
        });
    },
    methods: {
        algoliaPlacesChange: function (suggestion) {
            this.profilFormData.adresse = {
                codePostal: suggestion.postcode,
                commune: suggestion.name,
                pays: suggestion.country
            };
        },
        algoliaPlacesClear: function () {
            this.profilFormData.adresse = {
                codePostal: null,
                commune: null,
                pays: null
            };
        },
        validerProfil: function () {
            var erreur = false;
            for (var key in this.profilFormErrors) {
                if (this.profilFormErrors.hasOwnProperty(key)) {
                    this.profilFormErrors[key] = [];

                    if (!this.profilFormData[key]) {
                        this.profilFormErrors[key] = ["Veuillez saisir une valeur pour ce champ"];
                        erreur = true;
                    }
                    if (!this.profilFormData.adresse.codePostal ||
                        !this.profilFormData.adresse.commune ||
                        !this.profilFormData.adresse.pays) {
                        this.profilFormErrors.adresse = ["Veuillez saisir une valeur pour ce champ"];
                        erreur = true;
                    }
                }
            }

            if (!erreur) {
                document.querySelector('#profilForm').submit();
            }
        }
    }
});