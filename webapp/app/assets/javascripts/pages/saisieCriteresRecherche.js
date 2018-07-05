$(document).ready(function(){

    // On cache les métiers : on affiche uniquement les secteurs d'activité
    $(".metiers-container").hide();

    $("#rechercheAutreMetier-oui").each(function (i) {
        $('#js-critere-metiers').toggle($(this).prop("checked"));
    });

    $("input[type='radio'][name='rechercheAutreMetier']").click(function (i) {
        $('#js-critere-metiers').toggle($(this).prop("value") === "true");
    });

    // Initialisation des secteurs d'activités
    $("input[type='checkbox'][name='secteurActivite']").each(function(i) {
        var secteurActivite = $(this);
        var labelSecteurActivite = $("label[for='" + secteurActivite.attr("id") + "']");
        var compteurSecteurActivite = labelSecteurActivite.find(".compteur-metiers-selectionnes");
        var nbMetiers = secteurActivite.parent().find("input[type='checkbox'][name='listeMetiersRecherches[]']:checked").length;

        var avecMetiers = nbMetiers > 0;
        labelSecteurActivite.toggleClass("labelSecteurActivite-sans-focus-avec-metiers", avecMetiers);
        compteurSecteurActivite.toggle(avecMetiers);
        compteurSecteurActivite.html(nbMetiers);
    });

    $("input[type='checkbox'][name='secteurActivite']").click(function(i) {
        var clickedSecteurActivite = $(this);
        var labelClickedSecteurActivite = $("label[for='" + clickedSecteurActivite.attr("id") + "']");
        var compteurClickedSecteurActivite = labelClickedSecteurActivite.find(".compteur-metiers-selectionnes");
        var metiersContainer = clickedSecteurActivite.siblings(".metiers-container");

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
        $("input[type='checkbox'][name='secteurActivite'][id!='" + clickedSecteurActivite.attr("id") + "']:checked").each(function(i) {
            var unclickedSecteurActivite = $(this);
            var unclickedLabelClickedSecteurActivite = $("label[for='" + unclickedSecteurActivite.attr("id") + "']");

            unclickedSecteurActivite.siblings(".metiers-container").hide();
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
});