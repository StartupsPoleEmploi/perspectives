package fr.poleemploi.perspectives.recruteur.commentaire.domain

import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}

case class CommentaireListeCandidats(nomRecruteur: Nom,
                                     prenomRecruteur: Prenom,
                                     raisonSociale: String,
                                     contexteRecherche: ContexteRecherche,
                                     commentaire: String) {

  def labelSecteurActiviteRecherche: Option[String] = contexteRecherche.secteurActivite.map(_.label)

  def labelMetierRecherche: Option[String] = contexteRecherche.metier.map(_.label)

  def labelLocalisationRecherche: Option[String] = contexteRecherche.localisation
}
