import Vue from 'vue';
import $ from 'jquery';
import Pagination from '../../composants/Pagination.vue';
import '../../commun/filters.js';

new Vue({
    el: '#adminConseiller',
    components: {
        'pagination': Pagination
    },
    data: function () {
        return {
            rechercheFormData: {
                csrfToken: jsData.csrfToken,
                codesDepartement: [],
                codePostal: null,
                rechercheParLieuHabitation: '',
                rechercheParDateInscription: '',
                dateDebut: null,
                dateFin: null,
            },
            rechercheCandidatsFormData: {
                codeSecteurActivite: '',
                rechercheParSecteurMrs: ''
            },
            rechercheRecruteursFormData: {
                typeRecruteur: '',
                contactParCandidats: null
            },
            rechercheCandidats: {
                resultats: [],
                nbResultatsTotal: 0,
                nbParPage: 20,
                pages: []
            },
            rechercheRecruteurs : {
                resultats: [],
                nbResultatsTotal: 0,
                nbParPage: 20,
                pages: []
            },
            codeRegion: '',
            regions: jsData.regions,
            departements: jsData.departements,
            secteursActivites: jsData.secteursActivites,
            typesRecruteur: [
                {value: 'ENTREPRISE', label: 'Une entreprise'},
                {value: 'AGENCE_INTERIM', label: 'Une agence d\'interim'},
                {value: 'ORGANISME_FORMATION', label: 'Un organisme de formation'}
            ],
            display: {
                resultats: 'candidats',
                chargement: false
            }
        }
    },
    computed: {
        departementsFiltres: function() {
            if (this.codeRegion) {
                var self = this;
                return this.departements.filter(function(e) {
                    return e.codeRegion === self.codeRegion;
                });
            }
            else {
                return this.departements;
            }
        }
    },
    methods: {
        selectionnerDepartements: function() {
            var self = this;
            this.rechercheFormData.codesDepartement = this.departements.filter(function(e) {
                return e.codeRegion === self.codeRegion;
            }).map(function(d) {
                return d.code;
            });
        },
        nouvelleRechercheCandidats: function() {
            var self = this;
            this.rechercherCandidats(null).done(function(response)  {
                self.rechercheCandidats.nbResultatsTotal = response.nbCandidatsTotal;
                self.rechercheCandidats.pages = response.pageSuivante ? [null, response.pageSuivante] : [];
                self.$refs.paginationCandidats.pageChargee(1);
            });
        },
        paginerCandidats: function(index) {
            var page = this.rechercheCandidats.pages[index - 1];
            var self = this;
            this.rechercherCandidats(page).done(function(response)  {
                if (index === self.rechercheCandidats.pages.length && response.pageSuivante) {
                    self.rechercheCandidats.pages.push(response.pageSuivante);
                }
                self.$refs.paginationCandidats.pageChargee(index);
            });
        },
        rechercherCandidats: function(page) {
            var self = this;
            var formData = $('#rechercheCandidatsForm').serializeArray();
            if (page) {
                formData.push({name: "page.dateInscription", value: page.dateInscription});
                formData.push({name: "page.candidatId", value: page.candidatId});
            }

            return $.ajax({
                type: 'POST',
                url: '/conseiller/rechercherCandidats',
                data: formData,
                dataType: 'json',
                beforeSend: function () {
                    self.display.chargement = true;
                }
            }).done(function (response) {
                self.rechercheCandidats.resultats = response.candidats;
            }).always(function () {
                self.display.chargement = false;
            });
        },
        nouvelleRechercheRecruteurs: function() {
            var self = this;
            this.rechercherRecruteurs(null).done(function(response)  {
                self.rechercheRecruteurs.nbResultatsTotal = response.nbRecruteursTotal;
                self.rechercheRecruteurs.pages = response.pageSuivante ? [null, response.pageSuivante] : [];
                self.$refs.paginationRecruteurs.pageChargee(1);
            });
        },
        paginerRecruteurs: function(index) {
            var page = this.rechercheRecruteurs.pages[index - 1];
            var self = this;
            this.rechercherRecruteurs(page).done(function(response)  {
                if (index === self.rechercheRecruteurs.pages.length && response.pageSuivante) {
                    self.rechercheRecruteurs.pages.push(response.pageSuivante);
                }
                self.$refs.paginationRecruteurs.pageChargee(index);
            });
        },
        rechercherRecruteurs: function(page) {
            var self = this;
            var formData = $('#rechercheRecruteursForm').serializeArray();
            if (page) {
                formData.push({name: "page.dateInscription", value: page.dateInscription});
                formData.push({name: "page.recruteurId", value: page.recruteurId});
            }

            return $.ajax({
                type: 'POST',
                url: '/conseiller/rechercherRecruteurs',
                data: formData,
                dataType: 'json',
                beforeSend: function () {
                    self.display.chargement = true;
                }
            }).done(function (response) {
                self.rechercheRecruteurs.resultats = response.recruteurs;
            }).always(function () {
                self.display.chargement = false;
            });
        },
    }
});
