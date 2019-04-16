<template>
    <form method="POST" enctype="multipart/form-data" class="row"
          v-bind:id="id" v-bind:action="action" v-on:submit.prevent="telecharger">
        <input type="hidden" name="csrfToken" v-bind:value="csrfToken" />
        <div class="depotCV col col-lg-9 text-center py-4">
            <input class="depotCV-input" name="cv" type="file"
                   v-bind:id="'input-CV-' + id"
                   v-bind:accept="typesMediasValides.join(',')"
                   v-on:change="onChange"/>
            <div v-show="display.ajout">
                <div>
                    <img alt="Ajouter CV" src="/assets/images/composants/depotCV/ajouter.svg" />
                </div>
                <label v-bind:for="'input-CV-' + id" class="bouton bouton--blancSurVert depotCV-bouton mb-0">J'ajoute mon CV</label>
                <p class="texte-noir-50 font-size-xs mb-0 mt-4">Fichiers acceptés : {{extensionsValides.join(", ")}} ({{tailleMaxLabel}} max)</p>
            </div>
            <div v-show="display.telechargement">
                <div>
                    <img alt="Deposer CV" src="/assets/images/commun/chargement.gif" height="48" width="48"/>
                </div>
            </div>
            <div v-show="display.telechargementSucces">
                <p class="texte-noir font-size-sm">Parfait !</p>
                <div>
                    <img alt="CV ajouté" src="/assets/images/composants/depotCV/fichier-ajoute.svg" />
                </div>
                <p class="my-3 texte-noir font-size-sm">{{nomFichier}}</p>
                <label v-bind:for="'input-CV-' + id" class="bouton bouton--blancSurVert depotCV-bouton mb-0">Je modifie mon CV</label>
            </div>
            <div v-show="erreurs.length > 0">
                <div>
                    <img alt="Erreur fichier" src="/assets/images/composants/depotCV/erreur.svg" />
                </div>
                <p class="my-3 texte-erreur font-size-sm" v-for="erreur in erreurs">{{erreur}}</p>
                <label v-bind:for="'input-CV-' + id" class="bouton bouton--blancSurVert depotCV-bouton mb-0">J'essaye de nouveau</label>
            </div>
        </div>
    </form>
</template>

<script>
import $ from 'jquery';

export default {
    props: {
        action: String,
        csrfToken: String,
        extensionsValides: Array,
        typesMediasValides: Array,
        tailleMaxInBytes: Number,
        tailleMaxLabel: String
    },
    data: function() {
        return {
            id: null,
            nomFichier: null,
            erreurs: [],
            display: {
                ajout: true,
                telechargement: false,
                telechargementSucces: false
            }
        }
    },
    mounted () {
        this.id = 'depotCV-' + this._uid
    },
    methods: {
        onChange: function(e) {
            this.ajouter(e.target.files[0]);
        },
        ajouter: function(fichier) {
            this.erreurs = this.valider(fichier);

            if (this.erreurs.length === 0) {
                this.nomFichier = fichier.name.length > 30 ? fichier.name.substring(0, 30) + "..." : fichier.name;
                this.telecharger();
            }
            this.display.ajout = false;
            this.display.telechargementSucces = false;
        },
        valider: function(file) {
            var erreurs = [];
            if (this.tailleMaxInBytes !== 0 && file.size > this.tailleMaxInBytes) {
                erreurs.push("Votre fichier est trop lourd : " + this.tailleMaxLabel + " max");
            }
            if (!this.typesMediasValides.includes(file.type)) {
                erreurs.push("Le format n'est pas reconnu, assurez-vous que votre fichier soit dans l'un des formats suivants : " + this.extensionsValides.join(", "));
            }
            return erreurs;
        },
        telecharger: function() {
            this.display.ajout = false;
            this.display.telechargement = true;
            var self = this;
            $.ajax({
                url: '/candidat/cv',
                data: new FormData(document.querySelector('#' + this.id)),
                cache: false,
                contentType: false,
                processData: false,
                method: 'POST',
                type: 'POST'
            }).done(function (response) {
                self.display.telechargementSucces = true;
                document.getElementById(self.id).reset();
                self.display.telechargement = false;
            }).fail(function (jqXHR) {
                if (jqXHR.status === 400) {
                    self.erreurs = [jqXHR.responseText];
                } else {
                    self.erreurs = ["Désolé, nous avons rencontré un problème lors du téléchargement. Veuillez réessayer, merci !"]
                }
                document.getElementById(self.id).reset();
                self.display.telechargement = false;
            });
        }
    }
}
</script>