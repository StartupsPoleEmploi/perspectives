import Vue from 'vue';
import $ from 'jquery';
import 'bootstrap/js/dist/modal';
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
            ],
            urlVideoEmission: 'https://www.youtube.com/embed/IkCyCvJxGTo?autoplay=1',
            urlVideoMRS: 'https://www.youtube.com/embed/VpQOnDxUQek?ecver=2&autoplay=1'
        }
    },
    mounted: function () {
        var modaleVideoYoutube = $("#js-modaleVideo");
        var videoMRSYoutube = $("#js-videoMRSYoutube");

        modaleVideoYoutube.on("show.bs.modal", function (event) {
            var button = $(event.relatedTarget);
            videoMRSYoutube.attr("src", button.attr('data-urlVideo'));
        });
        modaleVideoYoutube.on("hidden.bs.modal", function () {
            videoMRSYoutube.attr("src", "");
        });
    }
});