package fr.poleemploi.perspectives.domain

case class Habilete(label: String)

object Habilete {

  val CREER = Habilete("Créer, innover, inventer")
  val RELATION_SERVICE = Habilete("Agir dans une relation de service")
  val MAINTIENT_ATTENTION = Habilete("Maintenir son attention dans la durée")
  val NEGOCIER = Habilete("Négocier")
  val PRISE_D_INITIATIVES = Habilete("Prendre des initiatives et être autonome")
  val RESPECT_NORMES_ET_CONSIGNES = Habilete("Respecter des normes et des consignes")
  val TRAVAIL_EN_EQUIPE = Habilete("Travailler en équipe")
  val TRAVAIL_SOUS_TENSION = Habilete("Travailler sous tension")
  val RECEUIL_ANALYSE_DONNEES = Habilete("Recueillir et analyser des données")
  val DEXTERITE = Habilete("Exécuter des gestes avec dextérité")
  val REPRESENTATION_PROCESSUS = Habilete("Se représenter un processus")
  val REPRESENTATION_ESPACE = Habilete("Se représenter un objet dans l'espace")
  val AFFIRMATION = Habilete("S'affirmer et faire face")
  val ADAPTATION = Habilete("S'adapter au changement")
  val ORGANISER = Habilete("Organiser")
  val MANAGER = Habilete("Manager une équipe")
  val COMMUNIQUER = Habilete("Communiquer")
}