module.exports = {
  outputDir: '../public', // seulement utilisé lors d'un build de production

  publicPath: '/assets', // alignement avec les routes de Play pour les assets

  pages: {
    conseillerAdmin: {
      entry: 'src/pages/conseiller/admin.js',
      template: 'static/index.html',
      filename: 'admin.html'
    }
  }
}
