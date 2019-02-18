import $ from 'jquery';

$(document).ready(function() {
    var iconeOuverture = $(".burgerMenu-iconeOuverture");
    var iconeFermeture = $(".burgerMenu-iconeFermeture");
    var logos = $(".navigation-conteneurLogo");
    var navigationConteneur = $(".navigation-conteneurMenu");

    iconeOuverture.click(function() {
        $(this).hide();
        logos.hide();
        iconeFermeture.show();
        navigationConteneur.show();
    });
    iconeFermeture.click(function() {
        $(this).hide();
        logos.show();
        iconeOuverture.show();
        navigationConteneur.hide();
    });

    var menuConnexion = $(".menuConnexion");
    var menuConnexionActions = $(".menuConnexionActions");

    menuConnexion.click(function() {
        menuConnexionActions.toggle();
    });
    menuConnexionActions.mouseleave(function() {
        $(this).hide();
    });
});