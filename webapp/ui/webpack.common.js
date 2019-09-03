const path = require('path');
const VueLoaderPlugin = require('vue-loader/lib/plugin');

module.exports = {
    entry: {
        candidatDepotCV: './src/pages/candidat/depotCV.js',
        candidatLanding: './src/pages/candidat/landing.js',
        candidatRechercheOffres: './src/pages/candidat/rechercheOffres.js',
        candidatSaisieCriteresRecherche: './src/pages/candidat/saisieCriteresRecherche.js',
        conseillerAdmin: './src/pages/conseiller/admin.js',
        infosLegales: './src/pages/infosLegales.js',
        menuNavigation: './src/modules/menuNavigation.js',
        recruteurLanding: './src/pages/recruteur/landing.js',
        recruteurProfil: './src/pages/recruteur/profil.js',
        recruteurRechercheCandidats: './src/pages/recruteur/rechercheCandidats.js'
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