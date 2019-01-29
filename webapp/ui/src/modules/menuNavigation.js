"use strict";

$(document).ready(function() {
    var iconeOuverture = $(".menuNavigationHeader-iconeOuverture");
    var iconeFermeture = $(".menuNavigationHeader-iconeFermeture");
    var menuNavigationActions = $(".menuNavigationActions");
    var menuNavigationResponsive = $(".menuNavigation-js-responsive");

    $(".menuNavigationHeader").click(function() {
        menuNavigationActions.toggle();

        if (menuNavigationResponsive.is(":visible")) {
            var isIconeOuvertureVisible = iconeOuverture.is(":visible");
            iconeOuverture.toggle();
            iconeFermeture.toggle(isIconeOuvertureVisible);
        }
    });
});