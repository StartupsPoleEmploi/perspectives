Vue.filter('capitalize', function (value) {
    if (!value) return '';
    value = value.toString();
    return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
});

Vue.filter('boolean', function (value) {
    if (value) return 'Oui';
    return 'Non';
});

Vue.filter('date', function (value) {
    return new Date(value).toLocaleDateString();
});

Vue.filter('statutDemandeurEmploi', function (value) {
    if (value === statutDemandeurEmploi.NON_DEMANDEUR_EMPLOI) {
        return "Non";
    } else if (value === statutDemandeurEmploi.DEMANDEUR_EMPLOI) {
        return "Oui";
    } else {
        return "";
    }
});

Vue.filter('typeRecruteur', function (value) {
    if (value === typeRecruteur.ENTREPRISE) {
        return "Entreprise";
    } else if (value === typeRecruteur.AGENCE_INTERIM) {
        return "Agence d'interim";
    } else if (value === typeRecruteur.ORGANISME_FORMATION) {
        return "Organisme de formation";
    } else {
        return "";
    }
});