package fr.poleemploi.perspectives.recruteur.commentaire.domain

case class ContexteRecherche(secteurActivite: Option[String],
                             metier: Option[String],
                             localisation: Option[String])
