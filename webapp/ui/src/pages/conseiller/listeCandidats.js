import Vue from 'vue';
import $ from 'jquery';
import '../../composants/pagination.js';
import '../../commun/filters.js';

var app = new Vue({
    el: '#listeCandidats',
    data: function () {
        return {
            csrfToken: jsData.csrfToken,
            candidats: jsData.candidats,
            pagesInitiales: jsData.pagesInitiales,
            nbCandidatsParPage: jsData.nbCandidatsParPage,
            codeROMEs: jsData.codeROMEs.result,
            mrsCandidatFormData: {},
            mrsCandidatFormErrors: {},
            afficherCandidats: true,
            afficherMRSCandidatForm: false,
            profilCandidatCourant: null
        }
    },
    methods: {
        chargerPageSuivante: function (critere) {
            this.paginerCandidats(critere).done(function (response) {
                app.candidats = response.candidats;
                app.$refs.pagination.pageSuivanteChargee(response.candidats.length, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function (critere, index) {
            this.paginerCandidats(critere).done(function (response) {
                app.candidats = response.candidats;
                app.$refs.pagination.pagePrecedenteChargee(index);
            });
        },
        paginerCandidats: function (critere) {
            return $.ajax({
                type: 'POST',
                url: '/conseiller/paginerCandidats',
                data: [
                    {name: "csrfToken", value: this.csrfToken},
                    {name: "dateInscription", value: critere.dateInscription},
                    {name: "candidatId", value: critere.candidatId}
                ],
                dataType: 'json'
            });
        },
        initialiserMRSCandidatFormAvecCandidat: function (candidat) {
            this.initialiserMRSCandidatForm();
            var form = this.mrsCandidatFormData;
            form.candidatId = candidat.candidatId;
            form.nomCandidat = candidat.nom;
            form.prenomCandidat = candidat.prenom;

            this.afficherMRSCandidatForm = true;
            this.afficherCandidats = false;
        },
        ajouterMRSCandidat: function (e) {
            var form = this.mrsCandidatFormData;

            $.ajax({
                type: 'POST',
                url: '/conseiller/ajouterMRSCandidat',
                data: [
                    {name: "csrfToken", value: this.csrfToken},
                    {name: "candidatId", value: form.candidatId},
                    {name: "codeROME", value: form.codeROME},
                    {name: "dateEvaluation", value: form.dateEvaluation},
                    {name: "codeDepartement", value: form.codeDepartement},
                    {name: "isDHAE", value: form.isDHAE},
                ],
                dataType: 'json'
            }).done(function (response) {
                app.afficherMRSCandidatForm = false;
                app.afficherCandidats = true;

                var candidatIndex = null;
                app.candidats.forEach(function(c, index, array) {
                    if (c.candidatId === form.candidatId) {candidatIndex = index}
                });
                app.candidats[candidatIndex].metiersValides.push({
                    metier: {codeROME: form.codeROME, label: "Rafraichir la page pour le label"},
                    departement: form.codeDepartement,
                    isDHAE: form.isDHAE
                });
            }).fail(function (jqXHR) {
                if (jqXHR.status === 400) {
                    app.mrsCandidatFormErrors = jqXHR.responseJSON;
                }
            });
        },
        annulerMRSCandidat: function() {
            this.afficherMRSCandidatForm = false;
            this.afficherCandidats = true;
            this.initialiserMRSCandidatForm();
        },
        initialiserMRSCandidatForm: function() {
            this.mrsCandidatFormData = {
                nomCandidat: null,
                prenomCandidat: null,
                candidatId: null,
                codeROME: null,
                codeDepartement: null,
                dateEvaluation: null,
                isDHAE: false
            };
            this.mrsCandidatFormErrors = {};
        },
        declarerRepriseEmploi: function(candidat) {
            $.ajax({
                type: 'GET',
                url: '/conseiller/declarerRepriseEmploi/' + candidat.candidatId,
                dataType: 'text'
            }).done(function () {
                app.candidats.find(function(c) {
                    return c.candidatId === candidat.candidatId;
                }).rechercheEmploi = false;
            });
        },
        estProfilCandidatCourant: function(candidat) {
            return candidat.candidatId === this.profilCandidatCourant;
        },
        toggleProfilCandidatCourant: function(candidat) {
            if (candidat.candidatId !== this.profilCandidatCourant) {
                this.profilCandidatCourant = candidat.candidatId;
            } else {
                this.profilCandidatCourant = null;
            }
        }
    }
});