import Vue from "vue";
import $ from 'jquery';
import 'bootstrap/js/dist/modal';
import places from 'places.js';

var app = new Vue({
    el: '#saisieCriteresRechercheCandidat',
    data: function () {
        return {
            criteresRechercheFormData: Object.assign({
                nouveauCandidat: false,
                contactRecruteur: null,
                contactFormation: null,
                numeroTelephone: null,
                localisation: {
                    commune: null,
                    codePostal: null,
                    latitude: null,
                    longitude: null,
                },
                rayonRecherche: null,
                tempsTravail: null,
                metiersValidesRecherches: [],
                metiersRecherches: [],
                domainesProfessionnelsRecherches: []
            }, jsData.criteresRechercheFormData),
            criteresRechercheFormErrors: Object.assign({
                contactRecruteur: [],
                contactFormation: [],
                numeroTelephone: [],
                localisation: [],
                rayonRecherche: [],
                tempsTravail: [],
                metiersValidesRecherches: [],
                metiersRecherches: [],
                domainesProfessionnelsRecherches: []
            }, jsData.criteresRechercheFormErrors),
            rayonsRecherche: [
                {value: 0, label: 'Dans ma ville'},
                {value: 10, label: 'Moins de 10km'},
                {value: 30, label: 'Moins de 30km'},
                {value: 50, label: 'Moins de 50km'}
            ],
            listeTempsTravail: [
                {value: 'MI_TEMPS', label: 'Mi-temps'},
                {value: 'PLEIN_TEMPS', label: 'Plein temps'}
            ],
            metiersValides: Object.assign([], jsData.metiersValides),
            secteursActivites: jsData.secteursActivites,
            display: {
                etape1: true,
                etape2: false,
                etape3: false,
                secteursActivites: {},
                metiersSelectionnesParSecteur: {}
            },
            algoliaPlacesConfig: jsData.algoliaPlacesConfig
        }
    },
    beforeMount: function() {
        var secteursActivites = {};
        var metiersSelectionnesParSecteur = {};
        jsData.secteursActivites.forEach(function(secteur) {
            secteursActivites[secteur.code] = false;
            metiersSelectionnesParSecteur[secteur.code] = [];
        });
        jsData.criteresRechercheFormData.metiersRecherches.forEach(function(codeROME) {
            var metier = jsData.secteursActivites.find(function(s) {
                return s.code === codeROME.charAt(0);
            }).metiers.find(function(m) {
                return m.codeROME === codeROME;
            });
            metiersSelectionnesParSecteur[codeROME.charAt(0)].push(metier);
        });
        this.display.secteursActivites = secteursActivites;
        this.display.metiersSelectionnesParSecteur = metiersSelectionnesParSecteur;
    },
    mounted: function () {
        var self = this;
        var placesAutocomplete = places({
            appId: self.algoliaPlacesConfig.appId,
            apiKey: self.algoliaPlacesConfig.apiKey,
            container: document.querySelector('#js-communeRecherche'),
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

        $('#js-modaleContactRecruteur').modal({show: false});
        $('#js-modaleMetiers').modal({show: false});
    },
    methods: {
        algoliaPlacesChange: function (suggestion) {
            this.criteresRechercheFormData.localisation = {
                codePostal: suggestion.postcode,
                commune: suggestion.name,
                latitude: suggestion.latlng.lat,
                longitude: suggestion.latlng.lng
            };
        },
        algoliaPlacesClear: function () {
            this.criteresRechercheFormData.localisation = {
                codePostal: null,
                commune: null,
                latitude: null,
                longitude: null
            };
        },
        validerEtape1: function () {
            this.criteresRechercheFormErrors.contactRecruteur = [];
            this.criteresRechercheFormErrors.contactFormation = [];
            this.criteresRechercheFormErrors.numeroTelephone = [];

            if (!this.criteresRechercheFormData.contactRecruteur) {
                this.criteresRechercheFormErrors.contactRecruteur = ["Veuillez saisir une valeur pour ce champ"];
            }
            if (!this.criteresRechercheFormData.contactFormation) {
                this.criteresRechercheFormErrors.contactFormation = ["Veuillez saisir une valeur pour ce champ"];
            }
            if ((this.criteresRechercheFormData.contactRecruteur === "true" || this.criteresRechercheFormData.contactFormation === "true") &&
                (!this.criteresRechercheFormData.numeroTelephone || this.criteresRechercheFormData.numeroTelephone === "" || this.criteresRechercheFormData.numeroTelephone.length < 10)) {
                this.criteresRechercheFormErrors.numeroTelephone = ["Vous devez renseigner un numéro de téléphone valide pour être contacté"];
            }

            if (this.criteresRechercheFormErrors.contactRecruteur.length === 0 &&
                this.criteresRechercheFormErrors.contactFormation.length === 0 &&
                this.criteresRechercheFormErrors.numeroTelephone.length === 0) {
                if (this.criteresRechercheFormData.contactRecruteur === "false" &&
                    this.criteresRechercheFormData.contactFormation === "false") {
                    $('#js-modaleContactRecruteur').modal('show');
                } else {
                    this.display.etape1 = false;
                    this.display.etape2 = true;
                }

                if (this.criteresRechercheFormData.nouveauCandidat) {
                    this.recupererLocalisation();
                    this.recupererMetiersValides();
                }
            }
        },
        validerEtape2: function () {
            this.criteresRechercheFormErrors.localisation = [];
            this.criteresRechercheFormErrors.rayonRecherche = [];

            if (!this.criteresRechercheFormData.localisation.latitude ||
                !this.criteresRechercheFormData.localisation.longitude) {
                this.criteresRechercheFormErrors.localisation = ["Veuillez saisir une valeur pour ce champ"];
            }
            if (!this.criteresRechercheFormData.rayonRecherche) {
                this.criteresRechercheFormErrors.rayonRecherche = ["Veuillez saisir une valeur pour ce champ"];
            }
            if (!this.criteresRechercheFormData.tempsTravail) {
                this.criteresRechercheFormErrors.tempsTravail = ["Veuillez saisir une valeur pour ce champ"];
            }

            if (this.criteresRechercheFormData.localisation.latitude &&
                this.criteresRechercheFormData.localisation.longitude &&
                this.criteresRechercheFormData.rayonRecherche !== null &&
                this.criteresRechercheFormData.tempsTravail) {
                this.display.etape2 = false;
                this.display.etape3 = true;
            }
        },
        retourEtape1: function () {
            this.display.etape1 = true;
            this.display.etape2 = false;
        },
        retourEtape2: function () {
            this.display.etape2 = true;
            this.display.etape3 = false;
        },
        accepterContactRecruteur: function () {
            this.criteresRechercheFormData.contactRecruteur = "true";

            if ((!this.criteresRechercheFormData.numeroTelephone || this.criteresRechercheFormData.numeroTelephone === "")) {
                this.criteresRechercheFormErrors.numeroTelephone = ["Vous devez renseigner un numéro de téléphone pour être contacté"];
            } else {
                this.display.etape1 = false;
                this.display.etape2 = true;
            }
        },
        continuerSansContactRecruteur: function() {
            this.display.etape1 = false;
            this.display.etape2 = true;
        },
        accepterAucunMetiers: function () {
            $('#criteresRechercheForm').submit();
        },
        resaisirMetiers: function() {
            $('#js-modaleMetiers').modal('hide');
        },
        recupererLocalisation: function() {
            $.ajax({
                type: "GET",
                url: "/candidat/localisation",
                dataType: "json"
            }).done(function (response) {
                app.criteresRechercheFormData.localisation = response.localisation;
            });
        },
        recupererMetiersValides: function() {
            $.ajax({
                type: "GET",
                url: "/candidat/metiers-valides",
                dataType: "json"
            }).done(function (response) {
                app.metiersValides = response.metiersValides;
            });
        },
        ajouterMetierSelectionne: function(metier, codeSecteur) {
            var checked = this.criteresRechercheFormData.metiersRecherches.indexOf(metier.codeROME) !== -1;
            if (checked) {
                this.display.metiersSelectionnesParSecteur[codeSecteur].push(metier);
            } else {
                this.display.metiersSelectionnesParSecteur[codeSecteur] = this.display.metiersSelectionnesParSecteur[codeSecteur].filter(function(m) {
                   return m.codeROME !== metier.codeROME;
                });
            }
        },
        deplierSecteur: function(codeSecteur) {
            this.display.secteursActivites[codeSecteur] = !this.display.secteursActivites[codeSecteur];
        },
        validerCriteres: function() {
            if (this.criteresRechercheFormData.metiersValidesRecherches.length === 0 &&
                this.criteresRechercheFormData.metiersRecherches.length === 0 &&
                this.criteresRechercheFormData.domainesProfessionnelsRecherches.length === 0) {
                $('#js-modaleMetiers').modal('show');
            } else {
                $('#criteresRechercheForm').submit();
            }
        }
    }
});