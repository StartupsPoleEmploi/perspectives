import Vue from 'vue';
import $ from 'jquery';
import Pagination from '../../composants/Pagination.vue';
import '../../commun/filters.js';

var app = new Vue({
    el: '#listeCandidats',
    components: {
        'pagination': Pagination
    },
    data: function () {
        return {
            csrfToken: jsData.csrfToken,
            candidats: jsData.candidats,
            pages: jsData.pages,
            nbCandidatsParPage: jsData.nbCandidatsParPage,
            codeROMEs: jsData.codeROMEs.result,
            mrsDHAEFormData: {
                candidatId: null,
                codeDepartement: null,
                codeROME: null,
                dateEvaluation: null
            },
            mrsDHAEFormErrors: {
                codeDepartement: [],
                codeROME: [],
                dateEvaluation: []
            },
            candidatCourant: {},
            display: {
                candidats: true,
                mrsDHAEForm: false
            }
        }
    },
    methods: {
        chargerPage: function (index) {
            var filtrePage = this.pages[index - 1];

            return $.ajax({
                type: 'POST',
                url: '/conseiller/paginerCandidats',
                data: [
                    {name: "csrfToken", value: this.csrfToken},
                    {name: "dateInscription", value: filtrePage.dateInscription},
                    {name: "candidatId", value: filtrePage.candidatId}
                ],
                dataType: 'json'
            }).done(function (response) {
                app.candidats = response.candidats;

                if (index === app.pages.length) {
                    if (response.candidats.length === app.nbCandidatsParPage) {
                        app.pages.push(response.pageSuivante);
                    }
                }
                app.$refs.pagination.pageChargee(index);
            });
        },
        initialiserMRSDHAEForm: function (candidat) {
            this.reinitialiserMRSDHAEForm();
            this.mrsDHAEFormData.candidatId = candidat.candidatId;

            this.display.candidats = false;
            this.display.mrsDHAEForm = true;
        },
        annulerMRSDHAE: function () {
            this.display.candidats = true;
            this.display.mrsDHAEForm = false;

            this.reinitialiserMRSDHAEForm();
        },
        reinitialiserMRSDHAEForm: function () {
            this.mrsDHAEFormData = {
                candidatId: null,
                codeDepartement: null,
                codeROME: null,
                dateEvaluation: null
            };
            this.mrsDHAEFormErrors = {
                codeDepartement: [],
                codeROME: [],
                dateEvaluation: []
            };
        },
        ajouterMRSCandidat: function (e) {
            $.ajax({
                type: 'POST',
                url: '/conseiller/ajouterMRSDHAECandidat',
                data: $('#mrsDHAEForm').serializeArray(),
                dataType: 'json'
            }).done(function () {
                var candidatIndex = app.candidats.findIndex(function(c) {
                    return c.candidatId === app.mrsDHAEFormData.candidatId;
                });
                app.candidats[candidatIndex].metiersValides.push({
                    metier: {codeROME: app.mrsDHAEFormData.codeROME, label: "Rafraichir la page pour le label"},
                    departement: app.mrsDHAEFormData.codeDepartement,
                    isDHAE: true
                });

                app.display.candidats = true;
                app.display.mrsDHAEForm = false;
                app.reinitialiserMRSDHAEForm();
            }).fail(function (jqXHR) {
                if (jqXHR.status === 400) {
                    app.mrsDHAEFormErrors = jqXHR.responseJSON;
                }
            });
        },
        estCandidatCourant: function (candidat) {
            return candidat.candidatId === this.candidatCourant.id;
        },
        toggleCandidatCourant: function (candidat) {
            if (candidat.candidatId !== this.candidatCourant.id) {
                this.candidatCourant = {
                    id: candidat.candidatId,
                    prenom: candidat.prenom,
                    nom: candidat.nom
                };
            } else {
                this.candidatCourant = {};
            }
        }
    }
});