import Vue from 'vue';
import Temoignages from '../../composants/Temoignages.vue';

new Vue({
    el: '#landingRecruteur',
    components: {
        'temoignages': Temoignages
    },
    data: function () {
        return {
            temoignages: [
                {
                    source: "Yvan B. - Synergie",
                    texte: "Ce nouveau dispositif nous a permis de mettre plusieurs candidats en poste et de pouvoir satisfaire nos clients."
                },
                {
                    source: "Matthieu V. - Manufacture Baizet",
                    texte: "Les profils correspondent, en termes d'aptitudes, aux profils que je recherche. Cela me permet d'être plus efficient. Je suis très satisfait de votre service."
                }
            ]
        }
    }
});