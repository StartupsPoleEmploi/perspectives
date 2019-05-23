import $ from 'jquery';

$(document).ready(function() {
    var iconeOuverture = $(".js-navigation-iconeOuverture");
    var iconeFermeture = $(".js-navigation-iconeFermeture");
    var logos = $(".js-navigation-conteneurLogo");
    var menu = $(".js-navigation-conteneurMenu");

    iconeOuverture.click(function() {
        $(this).hide();
        logos.hide();
        console.log("SHOW ICONE FERMETURE");
        iconeFermeture.show();
        menu.show();
    });
    iconeFermeture.click(function() {
        $(this).hide();
        logos.show();
        iconeOuverture.show();
        menu.hide();
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