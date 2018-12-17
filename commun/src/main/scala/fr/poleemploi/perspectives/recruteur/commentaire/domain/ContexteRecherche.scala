package fr.poleemploi.perspectives.recruteur.commentaire.domain

import fr.poleemploi.perspectives.commun.domain.{Metier, SecteurActivite}

case class ContexteRecherche(secteurActivite: Option[SecteurActivite],
                             metier: Option[Metier],
                             localisation: Option[String])
