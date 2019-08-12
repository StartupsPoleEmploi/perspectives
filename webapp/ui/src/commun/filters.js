import Vue from 'vue';
import statutsDemandeurEmploi from '../domain/candidat/statutDemandeurEmploi.js';
import typesRecruteur from '../domain/recruteur/typeRecruteur.js';
import typesContrats from '../domain/offre/typesContrats.js';
import tempsTravail from '../domain/candidat/tempsTravail.js';
import niveauxLangues from '../domain/candidat/niveauxLangues.js';

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
    return (value && statutsDemandeurEmploi[value]) ? statutsDemandeurEmploi[value].label : '';
});

Vue.filter('typeRecruteur', function (value) {
    return (value && typesRecruteur[value]) ? typesRecruteur[value] : '';
});

Vue.filter('typeContrat', function (value) {
    return (value && typesContrats[value]) ? typesContrats[value].label : value;
});

Vue.filter('tempsTravail', function (value) {
    return (value && tempsTravail[value]) ? tempsTravail[value].label : '';
});

Vue.filter('niveauLangue', function (value) {
    return (value && niveauxLangues[value]) ? niveauxLangues[value].label : '';
});

Vue.filter('numeroTelephone', function (value) {
    return value.replace(/(\d{2})/g, '$1 ').trim();
});