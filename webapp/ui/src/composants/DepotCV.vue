<template>
    <form method="POST" enctype="multipart/form-data" class="row"
          v-bind:id="id" v-bind:action="action" v-on:submit.prevent="telecharger">
        <input type="hidden" name="csrfToken" v-bind:value="csrfToken" />
        <div class="depotCV col col-lg-9 text-center py-4">
            <input class="hiddenInput" name="cv" type="file"
                   v-bind:id="'input-CV-' + id"
                   v-bind:accept="typesMediasValides.join(',')"
                   v-on:change="onChange"/>
            <template v-if="erreurs.length > 0">
                <img alt="Erreur fichier" src="/assets/images/composants/depotCV/erreur.svg" />
                <p class="my-3 texte-erreur font-size-sm" v-for="erreur in erreurs">{{erreur}}</p>
                <label v-bind:for="'input-CV-' + id" class="bouton bouton--blancSurVert depotCV-bouton mb-0">J'essaye de nouveau</label>
            </template>
            <template v-else-if="display.telechargement">
                <img alt="Deposer CV" src="/assets/images/commun/chargement.gif" height="48" width="48"/>
            </template>
            <template v-else-if="!fichier">
                <div>
                    <img alt="Ajouter CV" src="/assets/images/composants/depotCV/ajouter.svg" />
                </div>
                <label v-bind:for="'input-CV-' + id" class="bouton bouton--blancSurVert depotCV-bouton mb-0">J'ajoute mon CV</label>
                <p class="texte-noir-50 font-size-xs mb-0 mt-4">Fichiers acceptés : {{extensionsValides.join(", ")}} ({{tailleMaxLabel}} max)</p>
            </template>
            <template v-else>
                <p class="texte-noir font-size-sm" v-show="display.telechargementSucces">Votre CV a bien été téléchargé</p>
                <img alt="CV ajouté" src="/assets/images/composants/depotCV/fichier-ajoute.svg" />
                <p class="my-3 texte-noir font-size-sm">{{fichier}}</p>
                <a href="#"><label v-bind:for="'input-CV-' + id" class="mb-0 cursor-pointer">Modifier</label></a>
            </template>
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
        tailleMaxLabel: String,
        nomFichier: String
    },
    data: function() {
        return {
            id: null,
            fichier: this.nomFichier,
            erreurs: [],
            display: {
                telechargement: false,
                telechargementSucces: false
            }
        }
    },
    created: function() {
        this.id = 'depotCV-' + this._uid;
    },
    methods: {
        onChange: function(e) {
            this.ajouter(e.target.files[0]);
        },
        ajouter: function(fichier) {
            this.erreurs = this.valider(fichier);

            if (this.erreurs.length === 0) {
                this.fichier = fichier.name.length > 30 ? fichier.name.substring(0, 30) + "..." : fichier.name;
                this.telecharger();
            }
            this.display.telechargementSucces = false;
        },
        valider: function(fichier) {
            var erreurs = [];
            if (this.tailleMaxInBytes !== 0 && fichier.size > this.tailleMaxInBytes) {
                erreurs.push("Votre fichier est trop lourd : " + this.tailleMaxLabel + " max");
            }
            if (!this.typesMediasValides.includes(fichier.type)) {
                erreurs.push("Le format n'est pas reconnu, assurez-vous que votre fichier soit dans l'un des formats suivants : " + this.extensionsValides.join(", "));
            }
            return erreurs;
        },
        telecharger: function() {
            this.display.telechargement = true;
            var self = this;
            $.ajax({
                url: '/candidat/cv',
                data: new FormData(document.querySelector('#' + this.id)),
                cache: false,
                contentType: false,
                processData: false,
                method: 'POST'
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