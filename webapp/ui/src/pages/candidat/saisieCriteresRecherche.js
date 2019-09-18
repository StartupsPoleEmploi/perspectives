import Vue from 'vue';
import $ from 'jquery';
import _ from 'lodash';
import 'bootstrap/js/dist/modal';
import Places from '../../composants/Places.vue';
import ROME from '../../domain/metier/ROME';

new Vue({
    el: '#saisieCriteresRechercheCandidat',
    components: {
        'places': Places
    },
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
                {value: null, label: 'Dans ma ville'},
                {value: 10, label: 'Moins de 10km'},
                {value: 30, label: 'Moins de 30km'},
                {value: 50, label: 'Moins de 50km'}
            ],
            listeTempsTravail: [
                {value: 'TEMPS_PLEIN', label: 'Plein temps'},
                {value: 'TEMPS_PARTIEL', label: 'Temps partiel'}
            ],
            metiersValides: ROME.metiersParSecteur(Object.assign([], jsData.metiersValides)),
            secteursActivites: jsData.secteursActivites,
            secteursActivitesParCode: {},
            display: {
                etape1: true,
                etape2: false,
                etape3: false,
                secteursActivites: {},
                metiersSelectionnesParSecteur: {}
            },
            placesOptions: {
                appId: jsData.algoliaPlacesConfig.appId,
                apiKey: jsData.algoliaPlacesConfig.apiKey
            }
        }
    },
    created: function() {
        var secteursActivites = {};
        var secteursActivitesParCode = {};
        var metiersSelectionnesParSecteur = {};
        this.secteursActivites.forEach(function(secteur) {
            secteursActivites[secteur.code] = false;
            secteursActivitesParCode[secteur.code] = secteur;
            metiersSelectionnesParSecteur[secteur.code] = [];
        });
        this.criteresRechercheFormData.metiersRecherches.forEach(function(codeROME) {
            var metier = secteursActivitesParCode[ROME.codeSecteurActivite(codeROME)].metiers.find(function(m) {
                return m.codeROME === codeROME;
            });
            metiersSelectionnesParSecteur[ROME.codeSecteurActivite(codeROME)].push(metier);
        });
        this.secteursActivitesParCode = secteursActivitesParCode;
        this.display.secteursActivites = secteursActivites;
        this.display.metiersSelectionnesParSecteur = metiersSelectionnesParSecteur;
    },
    mounted: function () {
        $('#js-modaleContactRecruteur').modal({show: false});
        $('#js-modaleMetiers').modal({show: false});
    },
    methods: {
        placesChange: function (suggestion) {
            this.criteresRechercheFormData.localisation = {
                codePostal: suggestion.postcode,
                commune: suggestion.name,
                latitude: suggestion.latlng.lat,
                longitude: suggestion.latlng.lng
            };
        },
        placesClear: function () {
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
                    var self = this;
                    this.findLocalisation().done(function (response) {
                        self.criteresRechercheFormData.localisation = response.localisation;
                    });
                    this.findMetiersValides().done(function(response) {
                        // On coche tous les métiers validés pour un nouveau candidat
                        self.criteresRechercheFormData.metiersValidesRecherches = response.metiersValides.map(function(m) {
                            return m.codeROME;
                        });
                        self.metiersValides = ROME.metiersParSecteur(response.metiersValides);
                    });
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
            if (!this.criteresRechercheFormData.tempsTravail) {
                this.criteresRechercheFormErrors.tempsTravail = ["Veuillez saisir une valeur pour ce champ"];
            }

            if (this.criteresRechercheFormData.localisation.latitude &&
                this.criteresRechercheFormData.localisation.longitude &&
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
        findLocalisation: function() {
            return $.ajax({
                type: "GET",
                url: "/candidat/localisation",
                dataType: "json"
            });
        },
        findMetiersValides: function() {
            return $.ajax({
                type: "GET",
                url: "/candidat/metiers-valides",
                dataType: "json"
            });
        },
        hasMetiersValides: function() {
            return Object.keys(this.metiersValides).length > 0 && _.intersection(Object.keys(this.metiersValides), Object.keys(this.secteursActivitesParCode)).length > 0
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
