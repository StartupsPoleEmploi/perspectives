import Vue from 'vue';
import tracking from '../../commun/tracking';

new Vue({
    el: '#saisieDisponibilitesCandidat',
    data: function () {
        return {
            display: {
                confirmationDispo: jsData.confirmationDispo,
                confirmationNonDispo: jsData.confirmationNonDispo
            }
        }
    },
    created: function() {
        tracking.trackCommonActions();
    }
});
