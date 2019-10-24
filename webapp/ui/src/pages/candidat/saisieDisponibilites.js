import Vue from 'vue';
import axios from 'axios';
import afficherMessageSiPasDeMRS from "./candidatCommon";

new Vue({
    el: '#saisieDisponibilitesCandidat',
    data: function () {
        return {
            display: {
                confirmationDispo: false, // message de confirmation affiché lorsque le candidat est toujours disponible
                confirmationNonDispo: false, // message de confirmation affiché lorsque le candidat n'est plus disponible mais qu'il a precisé une date de dispo
                confirmationNonDispoSansDelai: false, // message de confirmation affiché lorsque le candidat n'est plus disponible et sans date de dispo
                erreurDisponibilites: false // message d'erreur générique
            },
            disponibilitesFormData: Object.assign({
                csrfToken: jsData.csrfToken,
                candidatEnRecherche: null,
                disponibiliteConnue: null,
                nbMoisProchaineDisponibilite: null,
                emploiTrouveGracePerspectives: null
            }, jsData.disponibilitesFormData),
            disponibilitesFormErrors: Object.assign({
                candidatEnRecherche: [],
                disponibiliteConnue: [],
                nbMoisProchaineDisponibilite: [],
                emploiTrouveGracePerspectives: []
            }, jsData.disponibilitesFormErrors)
        }
    },
    created: function() {
        afficherMessageSiPasDeMRS();
    },
    methods: {
        nettoyerErreursForm: function () {
            this.disponibilitesFormErrors = {
                candidatEnRecherche: [],
                disponibiliteConnue: [],
                nbMoisProchaineDisponibilite: [],
                emploiTrouveGracePerspectives: []
            };
        },
        doitAfficherFormulaire: function () {
            return !this.display.confirmationDispo
                && !this.display.confirmationNonDispo
                && !this.display.confirmationNonDispoSansDelai;
        },
        doitAfficherRemerciement: function () {
            return this.display.confirmationDispo
                || this.display.confirmationNonDispo
                || this.display.confirmationNonDispoSansDelai;
        },
        isCandidatPlusEnRecherche: function() {
            return this.disponibilitesFormData.candidatEnRecherche && this.disponibilitesFormData.candidatEnRecherche === 'false';
        },
        isDisponibiliteConnue: function() {
            return this.disponibilitesFormData.candidatEnRecherche && this.disponibilitesFormData.disponibiliteConnue === 'true';
        },
        modifierDisponibilites: function () {
            let self = this;

            if (self.validerFormulaireDisponibilites()) {
                let formData = new FormData(document.getElementById('disponibilitesCandidatsForm'));
                axios
                    .post('/candidat/disponibilites', formData)
                    .then(function (response) {
                        self.display.erreurDisponibilites = false;
                        self.nettoyerErreursForm();

                        if (response.data.candidatEnRecherche) {
                            self.display.confirmationDispo = true;
                            self.display.confirmationNonDispo = false;
                            self.display.confirmationNonDispoSansDelai = false;
                        } else {
                            self.display.confirmationDispo = false;
                            if (self.isDisponibiliteConnue()) {
                                self.display.confirmationNonDispo = true;
                                self.display.confirmationNonDispoSansDelai = false;
                            } else {
                                self.display.confirmationNonDispo = false;
                                self.display.confirmationNonDispoSansDelai = true;
                            }
                        }
                    })
                    .catch(function (error) {
                        if (error.response && error.response.status === 400) {
                            self.disponibilitesFormErrors = error.response.data;
                        } else {
                            self.disponibilitesFormErrors = {};
                            self.display.erreurDisponibilites = true;
                        }
                    });
            }
        },
        validerFormulaireDisponibilites: function () {
            this.nettoyerErreursForm();

            if (!this.disponibilitesFormData.candidatEnRecherche) {
                this.disponibilitesFormErrors.candidatEnRecherche = ["Veuillez saisir une valeur pour ce champ"];
            }

            if (this.isCandidatPlusEnRecherche()) {
                if (!this.disponibilitesFormData.disponibiliteConnue) {
                    this.disponibilitesFormErrors.disponibiliteConnue = ["Veuillez saisir une valeur pour ce champ"];
                }
                if (this.disponibilitesFormData.disponibiliteConnue === "true" && !this.disponibilitesFormData.nbMoisProchaineDisponibilite) {
                    this.disponibilitesFormErrors.nbMoisProchaineDisponibilite = ["Veuillez saisir une valeur pour ce champ"];
                }
                if (!this.disponibilitesFormData.emploiTrouveGracePerspectives) {
                    this.disponibilitesFormErrors.emploiTrouveGracePerspectives = ["Veuillez saisir une valeur pour ce champ"];
                }
            }

            return this.disponibilitesFormErrors.candidatEnRecherche.length === 0 &&
                this.disponibilitesFormErrors.disponibiliteConnue.length === 0 &&
                this.disponibilitesFormErrors.nbMoisProchaineDisponibilite.length === 0 &&
                this.disponibilitesFormErrors.emploiTrouveGracePerspectives.length === 0;
        }
    }
});
