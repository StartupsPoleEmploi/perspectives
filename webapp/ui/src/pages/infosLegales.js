import Vue from "vue";
import tracking from '../commun/tracking';

new Vue({
    el: '#infosLegales',
    data: function () {
        return {
            pagesCreditsPhotos: [
                {
                    titre: 'Accueil candidats',
                    photos: [
                        {credit: 'Noah Buscher on Unsplash', src: 'landing-candidat-1.jpg'},
                        {credit: 'RawPixel on Unsplash', src: 'landing-candidat-2.jpg'},
                        {credit: 'Fancry Crave on Unsplash', src: 'landing-candidat-3.jpg'},
                        {credit: 'Irina on Unsplash', src: 'landing-candidat-4.jpg'},
                        {credit: 'Rob Lambert on Unsplash', src: 'landing-candidat-5.jpg'},
                        {credit: 'Michael Browning on Unsplash', src: 'landing-candidat-6.jpg'},
                        {credit: 'LinkedIn Sales Navigator on Unsplash', src: 'landing-candidat-7.jpg'}
                    ]
                },
                {
                    titre: 'Accueil recruteurs',
                    photos: [
                        {credit: 'Sage Kirk on Unsplash', src: 'landing-recruteur-1.jpg'},
                        {credit: 'Tyler Nix on Unsplash', src: 'landing-recruteur-2.jpg'},
                        {credit: 'Benjamin Parker on Unsplash', src: 'landing-recruteur-3.jpg'},
                        {credit: 'Ryan Holloway on Unsplash', src: 'landing-recruteur-4.jpg'},
                        {credit: 'Edward Cisneros on Unsplash', src: 'landing-recruteur-5.jpg'},
                        {credit: 'Bruce Dixon on Unsplash', src: 'landing-recruteur-6.jpg'}
                    ]
                }
            ],
            display: {
                menu: null,
            }
        }
    },
    created: function () {
        tracking.trackCommonActions();
        location.hash === '#creditsPhotos' ? this.display.menu = 'creditsPhotos' : this.display.menu = 'cgu';
    }
});
