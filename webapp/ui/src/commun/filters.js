import Vue from 'vue';
import secteursActivites from '../domain/commun/secteurActivite.js';
import statutsDemandeurEmploi from '../domain/candidat/statutDemandeurEmploi.js';
import typesRecruteur from '../domain/recruteur/typeRecruteur.js';
import typesContrats from '../domain/offre/typesContrats.js';

Vue.filter('capitalize', function (value) {
    if (!value) return '';
    value = value.toString();
    return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
});

Vue.filter('take', function (value, n) {
    if (!value) return '';
    if (value.length > n) return value.substring(0, n) + '...';
    return value;
});

Vue.filter('boolean', function (value) {
    if (value) return 'Oui';
    return 'Non';
});

Vue.filter('date', function (value) {
    return new Date(value).toLocaleDateString();
});

Vue.filter('statutDemandeurEmploi', function (value) {
    return (value !== undefined && statutsDemandeurEmploi[value] !== undefined) ? statutsDemandeurEmploi[value].label : '';
});

Vue.filter('typeRecruteur', function (value) {
    return (value !== undefined && typesRecruteur[value] !== undefined) ? typesRecruteur[value] : '';
});

Vue.filter('typeContrat', function (value) {
    return (value !== undefined && typesContrats[value] !== undefined) ? typesContrats[value].label : value;
});

Vue.filter('secteurActivite', function (value) {
    return (value !== undefined && secteursActivites[value] !== undefined) ? secteursActivites[value].label : '';
});