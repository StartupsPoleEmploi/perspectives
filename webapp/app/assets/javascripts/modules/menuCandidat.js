$(document).ready(function(){

    var iconeOuverture = $(".menuCandidatHeader-iconeOuverture");
    var iconeFermeture = $(".menuCandidatHeader-iconeFermeture");
    var menuCandidatActions = $(".menuCandidatActions");
    var menuCandidatResponsive = $(".menuCandidat-js-responsive");

    $(".menuCandidatHeader").click(function(i) {
        menuCandidatActions.toggle();

        if (menuCandidatResponsive.is(":visible")) {
            var isIconeOuvertureVisible = iconeOuverture.is(":visible");
            iconeOuverture.toggle();
            iconeFermeture.toggle(isIconeOuvertureVisible);
        }
    });
});