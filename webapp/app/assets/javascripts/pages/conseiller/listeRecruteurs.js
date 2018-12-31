"use strict";

Vue.filter('capitalize', function (value) {
    if (!value) return '';
    value = value.toString();
    return value.charAt(0).toUpperCase() + value.slice(1);
});

Vue.filter('boolean', function (value) {
    if (value) return 'Oui';
    return 'Non';
});

Vue.filter('date', function (value) {
    return new Date(value).toLocaleDateString();
});
Vue.filter('typeRecruteur', function (value) {
    if (value === typeRecruteur.ENTREPRISE) {
        return "Entreprise";
    } else if (value === typeRecruteur.AGENCE_INTERIM) {
        return "Agence d'interim";
    } else if (value === typeRecruteur.ORGANISME_FORMATION) {
        return "Organisme de formation";
    } else {
        return "";
    }
});

var app = new Vue({
    el: '#listeRecruteurs',
    data: function() {
        return {
            csrfToken: jsData.csrfToken,
            recruteurs: jsData.recruteurs,
            pagesInitiales: jsData.pagesInitiales,
            nbRecruteursParPage: jsData.nbRecruteursParPage
        }
    },
    methods: {
        chargerPageSuivante: function(critere) {
            this.paginerRecruteurs(critere).done(function (response) {
                app.recruteurs = response.recruteurs;
                app.$refs.pagination.pageSuivanteChargee(response.recruteurs.length, response.pageSuivante);
            });
        },
        chargerPagePrecedente: function(critere, index) {
            this.paginerRecruteurs(critere).done(function (response) {
                app.recruteurs = response.recruteurs;
                app.$refs.pagination.pagePrecedenteChargee(index);
            });
        },
        paginerRecruteurs: function (critere) {
            return $.ajax({
                type: 'POST',
                url: '/conseiller/paginerRecruteurs',
                data: [
                    {name: "csrfToken", value: this.csrfToken},
                    {name: "dateInscription", value: critere.dateInscription},
                    {name: "recruteurId", value: critere.recruteurId}
                ],
                dataType: 'json'
            });
        }
    }
});