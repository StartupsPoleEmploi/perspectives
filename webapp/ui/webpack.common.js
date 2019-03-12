const path = require('path');
const VueLoaderPlugin = require('vue-loader/lib/plugin');

module.exports = {
    entry: {
        recruteurLanding: './src/pages/recruteur/landing.js',
        recruteurRechercheCandidat: './src/pages/recruteur/rechercheCandidat.js',
        conseillerListeCandidats: './src/pages/conseiller/listeCandidats.js',
        conseillerListeRecruteurs: './src/pages/conseiller/listeRecruteurs.js',
        candidatLanding: './src/pages/candidat/landing.js',
        candidatRechercheOffres: './src/pages/candidat/rechercheOffres.js',
        candidatSaisieCriteresRecherche: './src/pages/candidat/saisieCriteresRecherche.js',
        candidatDepotCV: './src/pages/candidat/depotCV.js',
        menuNavigation: './src/modules/menuNavigation.js'
    },
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, '../public/javascripts')
    },
    resolve: {
        alias: {
            'vue$': 'vue/dist/vue.esm.js'
        }
    },
    module: {
        rules: [
            { test: /\.vue$/, use: 'vue-loader' }
        ]
    },
    plugins: [
        new VueLoaderPlugin()
    ],
    optimization: {
        splitChunks: {
            chunks: 'initial',
            cacheGroups: {
                vendors: {
                    filename: '[name].bundle.js',
                    name: 'vendor',
                    test: /[\\/]node_modules[\\/]/,
                    chunks: 'initial',
                }
            }
        }
    }
};