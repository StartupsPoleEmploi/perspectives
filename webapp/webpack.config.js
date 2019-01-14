const path = require('path');

module.exports = {
    entry: {
        recruteurRechercheCandidat: './app/assets/javascripts/pages/recruteur/rechercheCandidat.js',
        conseillerListeCandidats: './app/assets/javascripts/pages/conseiller/listeCandidats.js',
        conseillerListeRecruteurs: './app/assets/javascripts/pages/conseiller/listeRecruteurs.js'
    },
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'app/assets/javascripts/pages')
    }
};
