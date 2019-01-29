"use strict";

var component = Vue.component('alerte-recruteur', {
    props:  {
        nbMaxAlertes: Number,
        alertesInitiales: Array,
        frequences: Array
    },
    data: function () {
        return {
            choisirFrequenceAlerte: false,
            listerAlertes: false,
            alertes: this.alertesInitiales,
            erreur: ''
        }
    },
    methods: {
        creerAlerte: function(frequence) {
            if (this.alertes.length === this.nbMaxAlertes) {
                this.erreur = "Vous avez atteint le nombre maximum d'alertes";
                this.fermerFrequences();
            } else {
                this.$emit("creer-alerte", frequence);
            }
        },
        onAlerteCree: function(alerte) {
            this.alertes.push(alerte);
            this.fermerFrequences();
            this.erreur = '';
        },
        supprimerAlerte: function(alerteId) {
            this.$emit("supprimer-alerte", alerteId);
        },
        onAlerteSupprimee: function(alerteId) {
            this.alertes = this.alertes.filter(function(e) {
                return e.id !== alerteId;
            });
            this.erreur = '';
        },
        selectionnerAlerte: function(alerteId) {
            this.$emit("selectionner-alerte", this.alertes.find(function(e) {
                return e.id === alerteId;
            }).criteres);
            this.fermerAlertes();
        },
        onErreur: function(erreur) {
            this.erreur = erreur;
        },
        ouvrirFrequences: function() {
            this.choisirFrequenceAlerte = !this.choisirFrequenceAlerte;
            this.fermerAlertes();
            this.erreur = '';
        },
        fermerFrequences: function() {
            this.choisirFrequenceAlerte = false;
        },
        ouvrirAlertes: function() {
            this.listerAlertes = !this.listerAlertes;
            this.fermerFrequences();
            this.erreur = '';
        },
        fermerAlertes: function() {
            this.listerAlertes = false;
        }
    },
    template:
    '<div id="alertesRecruteur">' +
        '<button title="Sélectionner des critères pour créer une alerte" ' +
                'class="alertesRecruteur-action boutonFrequence" ' +
                'v-on:click="ouvrirFrequences" v-on:blur="fermerFrequences">' +
            '<img alt="Creer alerte" height="18" width="18" class="creerAlerte" ' +
                  'src="/assets/images/composants/alerteRecruteur/creer-alerte.png" />' +
            'Créer une alerte pour cette recherche' +
        '</button>' +
        '<ul v-show="choisirFrequenceAlerte" class="listeChoix listeFrequences" v-on:mouseleave="fermerFrequences">' +
            '<li v-for="frequence in frequences" class="listeFrequences-item" v-on:mousedown="creerAlerte(frequence.value)">' +
            '{{ frequence.label }}' +
            '</li>' +
        '</ul>' +
        '<button class="alertesRecruteur-action boutonAlerte" ' +
                'v-on:click="ouvrirAlertes" v-on:blur="fermerAlertes">' +
            'Mes alertes ' +
            '<span class="compteurAlertes" v-show="alertes.length">{{alertes.length}}</span>' +
        '</button>' +
        '<div v-show="listerAlertes && !alertes.length" class="listeChoix sansAlertes" ' +
             'v-on:mouseleave="fermerAlertes">' +
            '<p><b>Vous n\'avez pas d\'alerte</b></p>' +
            '<span>Créez-vous des alertes et recevez les profils par email!</span>' +
        '</div>' +
        '<ul v-show="listerAlertes && alertes.length" class="listeChoix listeAlertes" ' +
            'v-on:mouseleave="fermerAlertes">' +
            '<li v-for="(alerte, index) in alertes" v-bind:class="[ index % 2 == 0 ? \'listeAlertes-item--pair\' : \'listeAlertes-item--impair\' ]" class="listeAlertes-item">' +
                '<div v-on:mousedown="selectionnerAlerte(alerte.id)">' +
                    '{{ alerte.intitule }}' +
                    '<div class="frequence">' +
                        '<img class="frequence-icone" alt="Frequence alerte" height="8" width="8" src="/assets/images/composants/alerteRecruteur/liste-alerte.png"/>' +
                        '<span>{{ alerte.frequence }}</span>' +
                    '</div>' +
                '</div>' +
                '<img alt="Supprimer alerte" height="21" width="19" class="listeAlertes-supprimer" ' +
                     'src="/assets/images/composants/alerteRecruteur/supprimer.png" ' +
                     'v-on:mousedown="supprimerAlerte(alerte.id)"/>' +
            '</li>' +
        '</ul>' +
        '<span v-show="erreur">{{erreur}}</span>' +
    '</div>'
});

export default component;