"use strict";

function intitule(alerte, metiers, secteursActivites) {
    var intitule = '';
    if (alerte.metier !== null && alerte.metier !== '') {
        intitule += metiers.find(function(m) {
            return m.codeROME === alerte.metier;
        }).label;
    } else if (alerte.secteurActivite !== null && alerte.secteurActivite !== '') {
        intitule += secteursActivites.find(function(s) {
            return s.code === alerte.secteurActivite;
        }).label;
    } else {
        intitule += "Candidats";
    }
    if (alerte.localisation.label !== null && alerte.localisation.label !== '') {
        intitule += " à " + alerte.localisation.label;
    }
    return intitule;
}

export function intituleAlerte(alerte, metiers, secteursActivites) {
    return intitule(alerte, metiers, secteursActivites);
}

export function buildAlerte(alerte, metiers, secteursActivites) {
    return {
        id: alerte.id,
        intitule: intituleAlerte(alerte, metiers, secteursActivites),
        frequence: alerte.frequence,
        criteres : {
            codeSecteurActivite: alerte.secteurActivite !== null ? alerte.secteurActivite : '',
            codeROME: alerte.metier !== null ? alerte.metier : '',
            localisation: alerte.localisation
        }
    };
}