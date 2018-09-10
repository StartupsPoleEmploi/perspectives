"use strict";

$(document).ready(function () {
    var secteursActivites = $("input[type='checkbox'][name='secteurActivite']");

    $("#rechercheAutreMetier-true").each(function () {
        $('#js-critere-metiers').toggle($(this).prop("checked"));
    });

    $("input[type='radio'][name='rechercheAutreMetier']").click(function () {
        $('#js-critere-metiers').toggle($(this).prop("value") === "true");
    });

    // Initialisation des secteurs d'activités : coche la case si tous les métiers sont sélectionnés
    secteursActivites.each(function () {
        var secteurActivite = $(this);

        var metiers = secteurActivite.siblings(".metiers").find("input[type='checkbox'][name='listeMetiersRecherches[]']");
        var metiersSelectionnes = secteurActivite.siblings(".metiers").find("input[type='checkbox'][name='listeMetiersRecherches[]']:checked");
        secteurActivite.prop("checked", metiers.length === metiersSelectionnes.length);
    });

    secteursActivites.click(function () {
        var clickedSecteurActivite = $(this);
        var isChecked = clickedSecteurActivite.prop("checked");

        clickedSecteurActivite.siblings(".metiers").find("input[type='checkbox'][name='listeMetiersRecherches[]']").each(function() {
            $(this).prop("checked", isChecked);
        });
    });

    if (window.FileReader) {
        var validerCriteres = $("#js-validerCriteres");
        var inputCV = $("#js-inputCV");
        var validerCV = $("#js-validerCV");
        var texteInitialValiderCV = validerCV.text();
        var erreursCV = $("#js-erreursCV");
        var succesCV = $("#js-succesCV");
        var nomFichier = $("#js-nomFichier");
        var nomFichierInitial = nomFichier.text();
        var indicationTailleMax = $("#js-indicationTaille");
        var progression = $("#js-progression");
        progression.hide();
        var barreProgression = $("#js-barreProgression");
        validerCV.hide();
        var tailleMaximumFichierBytes = 5 * 1000 * 1000;
        // FIXME : recupérer du back
        var mediaTypesValides = ["application/pdf", "application/vnd.oasis.opendocument.text", "image/jpeg", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"];

        inputCV.fileupload({
            url: '/candidat/modifierCV', // on utilise pas l'url du formulaire qui gère tous les champs en plus du CV
            dataType: 'json',
            formData: function (form) { // on envoit juste le token CSRF
                return form.serializeArray().filter(e => e.name === "csrfToken");
            },
            dropZone: null, // pas de drag and drop
            limitMultiFileUploads: 1,
            autoUpload: false,
            add: function (event, data) {
                var fichier = data.files[0];
                viderErreurs();
                viderSucces();
                var erreurs = [];

                if (fichier !== undefined) {
                    if (fichier.size > tailleMaximumFichierBytes) {
                        erreurs.push("Le fichier dépasse la taille maximale autorisée");
                    }
                    if (!mediaTypesValides.includes(fichier.type)) {
                        erreurs.push("Le type de fichier n'est pas valide");
                    }
                    nomFichier.text(fichier.name.length > 30 ? fichier.name.substring(0, 30) + "..." : fichier.name);
                } else {
                    nomFichier.text(nomFichierInitial);
                }

                if (erreurs.length > 0) {
                    erreurs.forEach(function (e) {
                        addErreur(e);
                    });
                    reinitialiser();
                } else {
                    validerCV.show();
                    indicationTailleMax.hide();
                    validerCV.click(function () {
                        validerCV.hide();
                        progression.show();
                        data.submit();
                        validerCriteres.prop("disabled", "disabled");
                    });
                }
            },
            progressall: function (event, data) {
                var progress = parseInt(data.loaded / data.total * 100, 10);
                barreProgression.text(progress + "%");
                barreProgression.css({"width": progress + "%"});
            },
            fail: function (event, data) {
                reinitialiser();
                addErreur("Une erreur est survenue pendant le téléchargement");
            },
            done: function (event, data) {
                reinitialiser();
                addSucces("Votre fichier a bien été envoyé");
            }
        });
    }

    function addErreur(text) {
        erreursCV.append("<div class='erreurs-item'>" + text + "</div>");
    }

    function viderErreurs() {
        erreursCV.html("");
    }

    function addSucces(text) {
        succesCV.append("<div class='succes-item'>" + text + "</div>");
    }

    function viderSucces() {
        succesCV.html("");
    }

    function reinitialiser() {
        progression.hide();
        barreProgression.text("");
        barreProgression.css({"width": 0});
        validerCV.unbind("click");
        validerCV.hide();
        validerCV.html(texteInitialValiderCV);
        indicationTailleMax.show();
        validerCriteres.prop("disabled", "");
    }
});