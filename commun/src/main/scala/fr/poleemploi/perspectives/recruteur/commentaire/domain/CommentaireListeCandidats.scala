package fr.poleemploi.perspectives.recruteur.commentaire.domain

case class CommentaireListeCandidats(nomRecruteur: String,
                                     prenomRecruteur: String,
                                     raisonSociale: String,
                                     contexteRecherche: ContexteRecherche,
                                     commentaire: String) {

  def labelSecteurActiviteRecherche: Option[String] = contexteRecherche.secteurActivite.map(_.label)

  def labelMetierRecherche: Option[String] = contexteRecherche.metier.map(_.label)

  def labelLocalisationRecherche: Option[String] = contexteRecherche.localisation
}
