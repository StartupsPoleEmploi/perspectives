"use strict";

$(document).ready(function () {
    var secteursActivites = $("input[type='checkbox'][name='secteurActivite']");

    // On cache les métiers : on affiche uniquement les secteurs d'activité
    $(".conteneurMetiers").hide();

    $("#rechercheAutreMetier-true").each(function () {
        $('#js-critere-metiers').toggle($(this).prop("checked"));
    });

    $("input[type='radio'][name='rechercheAutreMetier']").click(function () {
        $('#js-critere-metiers').toggle($(this).prop("value") === "true");
    });

    // Initialisation des secteurs d'activités
    secteursActivites.each(function () {
        var secteurActivite = $(this);
        var labelSecteurActivite = $("label[for='" + secteurActivite.attr("id") + "']");
        var compteurSecteurActivite = labelSecteurActivite.find(".compteur-metiers-selectionnes");
        var nbMetiers = secteurActivite.parent().find("input[type='checkbox'][name='listeMetiersRecherches[]']:checked").length;

        var avecMetiers = nbMetiers > 0;
        labelSecteurActivite.toggleClass("labelSecteurActivite-sans-focus-avec-metiers", avecMetiers);
        compteurSecteurActivite.toggle(avecMetiers);
        compteurSecteurActivite.html(nbMetiers);
    });

    secteursActivites.click(function () {
        var clickedSecteurActivite = $(this);
        var labelClickedSecteurActivite = $("label[for='" + clickedSecteurActivite.attr("id") + "']");
        var compteurClickedSecteurActivite = labelClickedSecteurActivite.find(".compteur-metiers-selectionnes");
        var metiersContainer = clickedSecteurActivite.siblings(".conteneurMetiers");

        // Tout ce qu'on sait c'est que les métiers sont cachés au démarrage
        metiersContainer.toggle();
        var isMetiersVisible = metiersContainer.is(":visible");

        // Compte le nombre de métiers selectionnés pour le secteur d'activité
        var nbMetiers = clickedSecteurActivite.parent().find("input[type='checkbox'][name='listeMetiersRecherches[]']:checked").length;

        // Gestion du deuxième click sur un même secteur : on affiche le nombre de métiers si le secteur est déselectionné et qu'il y en a plus que zéro, on le cache sinon
        var isUnselected = !isMetiersVisible && nbMetiers > 0;
        compteurClickedSecteurActivite.toggle(isUnselected);
        compteurClickedSecteurActivite.html(nbMetiers);

        // On modifie la classe indiquant une sélection en cours
        labelClickedSecteurActivite.toggleClass("labelSecteurActivite-avec-focus", isMetiersVisible);
        labelClickedSecteurActivite.toggleClass("labelSecteurActivite-sans-focus-avec-metiers", isUnselected);

        // On décoche le secteur d'activité précedemment coché si présent
        $("input[type='checkbox'][name='secteurActivite'][id!='" + clickedSecteurActivite.attr("id") + "']:checked").each(function () {
            var unclickedSecteurActivite = $(this);
            var unclickedLabelClickedSecteurActivite = $("label[for='" + unclickedSecteurActivite.attr("id") + "']");

            unclickedSecteurActivite.siblings(".conteneurMetiers").hide();
            unclickedSecteurActivite.prop("checked", false);

            // On modifie la classe
            unclickedLabelClickedSecteurActivite.removeClass("labelSecteurActivite-avec-focus");

            // On affiche le compteur si le nombre de métiers du secteur est supérieur à 0
            var compteurUnclickedSecteurActivite = unclickedLabelClickedSecteurActivite.find(".compteur-metiers-selectionnes");
            var nbMetiersUnclickedSecteurActivite = unclickedSecteurActivite.parent().find("input[type='checkbox'][name='listeMetiersRecherches[]']:checked").length;
            compteurUnclickedSecteurActivite.toggle(nbMetiersUnclickedSecteurActivite > 0);
            compteurUnclickedSecteurActivite.html(nbMetiersUnclickedSecteurActivite);

            unclickedLabelClickedSecteurActivite.toggleClass("labelSecteurActivite-sans-focus-avec-metiers", nbMetiersUnclickedSecteurActivite > 0);
        })
    });

    if (window.FileReader) {
        var validerCriteres = $("#js-validerCriteres");
        var inputCV = $("#js-inputCV");
        var validerCV = $("#js-validerCV");
        var texteInitialValiderCV = validerCV.text();
        var erreursCV = $("#js-erreursCV");
        var nomFichier = $("#js-nomFichier");
        var nomFichierInitial = nomFichier.text();
        var indicationTailleMax = $("#js-indicationTaille");
        var progressionUpload = $("#js-progression");
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
                var erreurs = [];

                if (fichier !== undefined) {
                    if (fichier.size > tailleMaximumFichierBytes) {
                        erreurs.push("Le fichier dépasse la taille maximale autorisée");
                    }
                    if (!mediaTypesValides.includes(fichier.type)) {
                        erreurs.push("Le type de fichier n'est pas valide");
                    }
                    nomFichier.text(fichier.name);
                } else {
                    nomFichier.text(nomFichierInitial);
                }

                if (erreurs.length > 0) {
                    erreurs.forEach(function(e) {
                        addErreur(e);
                    });
                    reinitialiser();
                } else {
                    validerCV.show();
                    indicationTailleMax.hide();
                    validerCV.click(function () {
                        validerCV.html("Envoi du CV...");
                        data.submit();
                        validerCriteres.prop("disabled", "disabled");
                    });
                }
            },
            progressall: function (event, data) {
                var progress = parseInt(data.loaded / data.total * 100, 10);
                progressionUpload.text(progress + "%");
            },
            fail: function (event, data) {
                reinitialiser();
                addErreur("Une erreur est survenue pendant le téléchargement");
            },
            done: function (event, data) {
                reinitialiser();
                viderErreurs();
            }
        });
    }

    function addErreur(text) {
        erreursCV.append("<div class='erreurs-item'>" + text + "</div>");
    }

    function viderErreurs() {
        erreursCV.html("");
    }

    function reinitialiser() {
        progressionUpload.text("");
        validerCV.unbind("click");
        validerCV.hide();
        validerCV.html(texteInitialValiderCV);
        indicationTailleMax.show();
        validerCriteres.prop("disabled", "");
    }
});