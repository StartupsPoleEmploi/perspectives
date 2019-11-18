import Vue from 'vue';
import Places from '../../composants/Places.vue';
import tracking from '../../commun/tracking';

new Vue({
    el: '#profilRecruteur',
    components: {
        'places': Places
    },
    data: function () {
        return {
            profilFormData: Object.assign({
                csrfToken: jsData.csrfToken,
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
            placesOptions: {
                appId: jsData.algoliaPlacesConfig.appId,
                apiKey: jsData.algoliaPlacesConfig.apiKey
            }
        }
    },
    created: function() {
        tracking.trackCommonActions();
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
    methods: {
        placesChange: function (suggestion) {
            this.profilFormData.adresse = {
                codePostal: suggestion.postcode,
                commune: suggestion.name,
                pays: suggestion.country
            };
        },
        placesClear: function () {
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
                tracking.sendEvent(tracking.Events.RECRUTEUR_PROFIL_MODIFIE, {
                    'is_creation': this.profilFormData.nouveauRecruteur
                });
                document.querySelector('#profilForm').submit();
            }
        }
    }
});
