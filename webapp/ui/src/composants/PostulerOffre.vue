<template>
    <div class="row align-items-center">
        <div class="col-12 col-md-9">
            <p class="mb-0 texte-noir font-weight-bold">Cette offre vous intéresse, postulez !</p>
            <p class="mb-0 texte-noir">Veuillez adresser votre CV et une lettre de motivation en précisant le numéro de l'offre : <span class="font-weight-bold">n°{{offreId}}</span></p>
        </div>
        <div class="col-12 col-md-3">
            <button type="button" class="bouton bouton-action bouton--blancSurVert w-100 gtm-candidat-detail-postuler"
                    v-show="!doitAfficherContact && !doitAfficherLienOrigineOffre()"
                    v-on:click="afficherContact()">Je contacte le recruteur
            </button>
            <a target="_blank" class="bouton bouton-action bouton--blancSurVert w-100 gtm-candidat-detail-postuler"
               v-show="doitAfficherLienOrigineOffre()"
               v-bind:href="urlOrigine"
               v-on:click="voirOffreSurPoleEmploi()">
                Voir l'offre sur Pôle Emploi
            </a>
            <div class="bg-vert-emeraude-light p-3 border-radius-sm texte-noir font-size-sm" v-show="doitAfficherContact && !doitAfficherLienOrigineOffre()">
                <p class="mb-2 font-weight-bold" v-show="contact.nom">{{contact.nom}}</p>
                <p class="mb-2" v-show="doitAfficherCoordonnees1()">{{contact.coordonnees1}}</p>
                <p class="mb-2" v-show="contact.coordonnees2">{{contact.coordonnees2}}</p>
                <p class="mb-2" v-show="contact.coordonnees3">{{contact.coordonnees3}}</p>
                <p class="mb-2" v-show="contact.telephone">Téléphone : {{contact.telephone}}</p>
                <p class="mb-2" v-show="contact.email">Courriel : {{contact.email}}</p>
                <p>
                    <a class="mb-2" target="_blank" v-show="contact.urlPostuler"
                       v-bind:href="contact.urlPostuler">
                        Postuler directement sur le site du recruteur
                    </a>
                </p>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        props: {
            offreId: String,
            urlOrigine: String,
            contact: {
                type: Object,
                default: function () {
                    return {};
                }
            },
            doitAfficherContact: {
                type: Boolean,
                default: function() {
                    return false;
                }
            }
        },
        methods: {
            doitAfficherCoordonnees1: function () {
                return this.contact && this.contact.coordonnees1 &&
                    this.contact.coordonnees1 !== this.contact.urlPostuler &&
                    this.contact.coordonnees1.indexOf(this.contact.email) === -1 &&
                    this.contact.coordonnees1.indexOf(this.contact.telephone) === -1;
            },
            doitAfficherLienOrigineOffre: function () {
                return this.contact && !this.contact.email &&
                    !this.contact.telephone &&
                    !this.contact.urlPostuler &&
                    !this.contact.coordonnees1;
            },
            afficherContact: function () {
                this.$emit('afficher-contact');
            },
            voirOffreSurPoleEmploi: function() {
                this.$emit('voir-offre-sur-pole-emploi');
            }
        }
    }
</script>
