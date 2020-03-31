<template>
  <div class="autocompletion">
    <div class="autocompletion__box inputText h-auto font-size-sm px-2 py-3"
      :class="{'autocompletion__recherche' : montrerResultats}">
      <img v-if="!estEnCoursDeChargement"
        class="autocompletion__icone"
        src="/assets/images/composants/autocompletion/rechercher.svg" />
      <img v-else
        class="autocompletion__icone"
        src="/assets/images/composants/autocompletion/chargement.svg" />
      <div class="autocompletion__inputs">
        <input
          class="texte-noir"
          v-model="affichage"
          :placeholder="placeholder"
          @click="rechercher"
          @input="rechercher"
          @keydown.enter="entrer"
          @keydown.tab="fermer"
          @keydown.up="haut"
          @keydown.down="bas"
          @keydown.esc="fermer"
          @focus="donnerFocus"
          @blur="retirerFocus"
          type="text"
          autocomplete="disabled" />
        <input :name="nom" type="hidden" :value="valeur" />
      </div>
      <span v-show="!estVide && !estEnCoursDeChargement && !aUneErreur" class="autocompletion--effacer" @click="effacer">
        <img class="autocompletion__icone"
             src="/assets/images/composants/autocompletion/effacer.svg">
      </span>
    </div>
    <ul v-show="montrerResultats" class="autocompletion__resultats">
      <slot name="resultats">
        <li v-if="aUneErreur"
          class="autocompletion__resultats__element autocompletion__resultats__element--erreur">
          {{ erreur }}
        </li>
        <template v-if="!aUneErreur">
          <li v-for="(resultat, cle) in resultats"
            :key="cle"
            @click.prevent="selectionner(resultat)"
            class="autocompletion__resultats__element"
            :class="{'autocompletion__selectionne' : estSelectionne(cle) }"
            v-html="formaterAffichage(resultat)"></li>
        </template>
        <li v-if="pasDeResultatsMessage"
          class="autocompletion__resultats__element autocompletion__pas-de-resultats">
          <slot name="pasDeResultats">Pas de résultats.</slot>
        </li>
      </slot>
    </ul>
  </div>
</template>

<script>
export default {
  props: {
    source: {
      type: String,
      required: true
    },
    placeholder: {
      default: 'Rechercher'
    },
    valeurInitiale: {
      type: String
    },
    affichageInitial: {
      type: String
    },
    nom: {
      type: String
    },
    proprieteResultats: {
      type: String
    },
    valeurResultats: {
      type: String
    },
    affichageResultats: {
      type: String
    },
    longueurMin: {
      type: Number,
      default: 1
    }
  },
  data () {
    return {
      valeur: null,
      affichage: null,
      resultats: null,
      indexSelectionne: null,
      chargement: false,
      estFocus: false,
      erreur: null,
      affichageSelectionne: null,
      eventListener: false,
    }
  },
  computed: {
    montrerResultats () {
      return Array.isArray(this.resultats) || this.aUneErreur
    },
    pasDeResultats () {
      return Array.isArray(this.resultats) && this.resultats.length === 0
    },
    pasDeResultatsMessage () {
      return this.pasDeResultats &&
        !this.estEnCoursDeChargement &&
        this.estFocus &&
        !this.aUneErreur
    },
    estVide () {
      return !this.affichage
    },
    estEnCoursDeChargement () {
      return this.chargement === true
    },
    aUneErreur () {
      return this.erreur !== null
    }
  },
  methods: {
    rechercher () {
      this.indexSelectionne = null
      if (!this.affichage || this.affichage.length < this.longueurMin) {
        this.resultats = null
        return
      }
      return this.rechercherSource(this.source + this.affichage)
    },
    rechercherSource (url) {
      if (!this.affichage) {
        this.resultats = []
        return
      }
      this.chargement = true
      this.setEventListener()
      this.requeter(url)
    },
    requeter (url) {
      let promise = fetch(url, {
        method: 'get'
      })
      return promise
        .then(reponse => {
          if (reponse.ok) {
            this.erreur = null
            return reponse.json()
          }
          throw new Error('Réponse réseau incorrecte.')
        })
        .then(reponse => {
          this.resultats = this.setResultats(reponse)
          this.chargement = false
        })
        .catch(erreur => {
          this.erreur = erreur.message
          this.chargement = false
        })
    },
    setResultats (reponse) {
      if (this.proprieteResultats && reponse[this.proprieteResultats]) {
        return reponse[this.proprieteResultats]
      }
      if (Array.isArray(reponse)) {
        return reponse
      }
      return []
    },
    selectionner (obj) {
      if (!obj) {
        return
      }
      this.valeur = (this.valeurResultats && obj[this.valeurResultats]) ? obj[this.valeurResultats] : obj.id
      this.affichage = this.formaterAffichage(obj)
      this.affichageSelectionne = this.affichage
      this.$emit('input', this.valeur)
      this.fermer()
    },
    formaterAffichage (obj) {
      if (!obj[this.affichageResultats]) {
        throw new Error(`"${this.affichageResultats}" non défini.`)
      }
      return obj[this.affichageResultats]
    },
    donnerFocus () {
      this.estFocus = true
    },
    retirerFocus () {
      this.estFocus = false
    },
    estSelectionne (key) {
      return key === this.indexSelectionne
    },
    haut () {
      if (this.indexSelectionne === null) {
        this.indexSelectionne = this.resultats.length - 1
        return
      }
      this.indexSelectionne = (this.indexSelectionne === 0) ? this.resultats.length - 1 : this.indexSelectionne - 1
    },
    bas () {
      if (this.indexSelectionne === null) {
        this.indexSelectionne = 0
        return
      }
      this.indexSelectionne = (this.indexSelectionne === this.resultats.length - 1) ? 0 : this.indexSelectionne + 1
    },
    entrer () {
      if (this.indexSelectionne === null) {
        return
      }
      this.selectionner(this.resultats[this.indexSelectionne])
    },
    effacer () {
      this.affichage = null
      this.valeur = null
      this.resultats = null
      this.erreur = null
    },
    fermer () {
      if (!this.valeur || !this.affichageSelectionne) {
        this.effacer()
      }
      if (this.affichageSelectionne !== this.affichage && this.valeur) {
        this.affichage = this.affichageSelectionne
      }
      this.resultats = null
      this.erreur = null
      this.removeEventListener()
    },
    setEventListener () {
      if (this.eventListener) {
        return false
      }
      this.eventListener = true
      document.addEventListener('click', this.clicExterieurListener, true)
      return true
    },
    removeEventListener () {
      this.eventListener = false
      document.removeEventListener('click', this.clicExterieurListener, true)
    },
    clicExterieurListener (event) {
      if (this.$el && !this.$el.contains(event.target)) {
        this.fermer()
      }
    }
  },
  mounted () {
    this.valeur = this.valeurInitiale
    this.affichage = this.affichageInitial
    this.affichageSelectionne = this.affichageInitial
  }
}
</script>