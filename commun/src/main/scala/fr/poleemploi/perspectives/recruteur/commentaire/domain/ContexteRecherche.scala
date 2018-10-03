package fr.poleemploi.perspectives.recruteur.commentaire.domain

import fr.poleemploi.perspectives.commun.domain.{Departement, Metier, SecteurActivite}

case class ContexteRecherche(secteurActivite: Option[SecteurActivite],
                             metier: Option[Metier],
                             departement: Option[Departement])
