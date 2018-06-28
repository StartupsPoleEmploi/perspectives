$(document).ready(function(){
    var formerCandidats = $("#js-formerCandidats");
    var boutonInscription = $("#js-boutonInscription");

    // Initialisation
    formerCandidats.each(function () {
        boutonInscription.prop("disabled", $(this).prop("checked") ? "" : "disabled");
    });

    formerCandidats.click(function () {
        boutonInscription.prop("disabled", $(this).prop("checked") ? "" : "disabled");
    });
});