// ROME : Répertoire Opérationnel des Métiers et des Emplois
var service = {
    codeSecteurActivite: function(codeROME) {
        return codeROME.charAt(0);
    },
    metiersParSecteur: function(metiers) {
        return metiers.reduce(function (acc, metier) {
            var key = service.codeSecteurActivite(metier.codeROME);
            acc[key] = acc[key] || [];
            acc[key].push(metier);
            return acc;
        }, {});
    }
};

export default service;