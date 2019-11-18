import Vue from 'vue';
import Temoignages from '../../composants/Temoignages.vue';
import Places from '../../composants/Places.vue';
import rayonsRechercheOffres from "../../domain/offre/rayonRecherche";
import tracking from '../../commun/tracking';

new Vue({
    el: '#landingCandidat',
    components: {
        'temoignages': Temoignages,
        'places': Places
    },
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
                    texte: "Je viens de décrocher un poste aux Sables d'Olonne en tant qu'aide à domicile. Je vous remercie pour votre investissement !"
                }
            ],
            rechercheOffresFormData: {
                lieuTravail: null,
                codePostal: null,
                rayonRecherche: 0,
            },
            rechercheOffresFormErrors: [],
            rayonsRechercheOffres: rayonsRechercheOffres,
            placesOptions: {
                appId: jsData.algoliaPlacesConfig.appId,
                apiKey: jsData.algoliaPlacesConfig.apiKey
            }
        }
    },
    created: function() {
        tracking.trackCommonActions();
    },
    methods: {
        placesChange: function(suggestion) {
            this.rechercheOffresFormData.codePostal = suggestion.postcode;
            this.rechercheOffresFormData.lieuTravail = suggestion.name;
        },
        placesClear: function() {
            this.rechercheOffresFormData.codePostal = null;
            this.rechercheOffresFormData.lieuTravail = null;
        },
        rechercherOffres: function() {
            this.rechercheOffresFormErrors = [];
            if (!this.rechercheOffresFormData.codePostal || this.rechercheOffresFormData.codePostal === '') {
                this.rechercheOffresFormErrors.push({champ: 'codePostal', label: 'Dites-nous où vous recherchez un emploi'});
            }

            if (this.rechercheOffresFormErrors.length === 0) {
                tracking.sendEvent(tracking.Events.CANDIDAT_RECHERCHE_OFFRE, {
                    'code_postal': this.rechercheOffresFormData.codePostal,
                    'lieu_recherche': this.rechercheOffresFormData.lieuTravail,
                    'rayon_recherche': this.rechercheOffresFormData.rayonRecherche
                });

                var params = [];
                params.push('codePostal=' + this.rechercheOffresFormData.codePostal);
                params.push('lieuTravail=' + this.rechercheOffresFormData.lieuTravail);
                params.push('rayonRecherche=' + this.rechercheOffresFormData.rayonRecherche);
                var uri = encodeURI(params.reduce(function(acc, param, index) {
                    return acc + (index === 0 ? '?' : '&') + param;
                }, '/candidat/offres'));
                window.location.assign(uri);
            }
        }
    }
});
