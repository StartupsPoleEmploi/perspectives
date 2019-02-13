import temoignages from '../../composants/temoignages.js';

var app = new Vue({
    el: '#landingCandidat',
    data: function () {
        return {
            temoignages: [
                {
                    source: "Rebecca S.",
                    texte: "Le lendemain de mon inscription, j'ai été contactée par un employeur ainsi qu'une agence d’intérim. On m'a déjà proposé un essai !"
                },
                {
                    source: "Géraldine R.",
                    texte: "Je reprends un emploi cet été en espérant plus pour la suite. Je suis très contente. Merci."
                },
                {
                    source: "Jessica H.",
                    texte: "Je viens de décrocher un poste aux Sables d'olonne en tant qu'aide à domicile. Je vous remercie pour votre investissement !"
                }
            ],
            lieuTravail: {
                label: '',
                latitude: null,
                longitude: null
            },
            rayonRecherche: null,
            rayonsRecherche: [10, 30, 50, 100],
            metier: null,
            metiers: []
        }
    },
    methods: {
        rechercherOffres: function() {
            var params = [];
            if (this.metier !== null && this.metier !== '') {
                params.push('metier=' + this.metier);
            }
            if (this.lieuTravail.label !== '') {
                params.push('localisation=' + this.lieuTravail.label);
            }
            if (this.rayonRecherche !== null && this.rayonRecherche !== '') {
                params.push('rayonRecherche=' + this.rayonRecherche);
            }
            if (params.length > 0) {
                var uri = encodeURI(params.reduce(function(acc, param, index) {
                    return acc + (index === 0 ? '?' : '&') + param;
                }, '/candidat/offres'));
                window.location.assign(uri);
            }
        }
    }
});