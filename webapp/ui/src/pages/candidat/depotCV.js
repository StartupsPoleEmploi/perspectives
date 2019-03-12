import Vue from "vue";
import DepotCV from '../../composants/DepotCV.vue';

var app = new Vue({
    el: '#depotCVCandidat',
    components: {
        'depot-cv': DepotCV
    },
    data: function () {
        return {
            nouveauCandidat: jsData.nouveauCandidat,
            avecCV: jsData.avecCV,
            typesMediasValides: jsData.typesMediasValides,
            extensionsValides: jsData.extensionsValides,
            tailleMaxInBytes: jsData.tailleMaxInBytes,
            tailleMaxLabel: jsData.tailleMaxLabel,
            csrfToken: jsData.csrfToken
        }
    }
});