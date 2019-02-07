const path = require('path');

module.exports = {
    entry: {
        recruteurLanding: './src/pages/recruteur/landing.js',
        recruteurRechercheCandidat: './src/pages/recruteur/rechercheCandidat.js',
        conseillerListeCandidats: './src/pages/conseiller/listeCandidats.js',
        conseillerListeRecruteurs: './src/pages/conseiller/listeRecruteurs.js',
        candidatLanding: './src/pages/candidat/landing.js',
        candidatListeOffres: './src/pages/candidat/listeOffres.js',
        candidatSaisieCriteresRecherche: './src/pages/candidat/saisieCriteresRecherche.js',
        menuNavigation: './src/modules/menuNavigation.js'
    },
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, '../public/javascripts')
    }
};