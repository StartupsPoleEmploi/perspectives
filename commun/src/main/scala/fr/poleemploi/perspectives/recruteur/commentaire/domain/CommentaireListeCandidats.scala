package fr.poleemploi.perspectives.recruteur.commentaire.domain

import fr.poleemploi.perspectives.commun.domain._

case class CommentaireListeCandidats(nomRecruteur: Nom,
                                     prenomRecruteur: Prenom,
                                     raisonSociale: String,
                                     contexteRecherche: ContexteRecherche,
                                     commentaire: String) {

  def secteurActivite: Option[String] = contexteRecherche.secteurActivite

  def metier: Option[String] = contexteRecherche.metier

  def localisation: Option[String] = contexteRecherche.localisation
}
