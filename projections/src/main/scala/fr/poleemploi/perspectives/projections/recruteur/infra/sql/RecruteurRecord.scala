package fr.poleemploi.perspectives.projections.recruteur.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}

case class RecruteurRecord(recruteurId: RecruteurId,
                           nom: Nom,
                           prenom: Prenom,
                           email: Email,
                           genre: Genre,
                           typeRecruteur: Option[TypeRecruteur],
                           raisonSociale: Option[String],
                           codePostal: Option[String],
                           commune: Option[String],
                           pays: Option[String],
                           numeroSiret: Option[NumeroSiret],
                           numeroTelephone: Option[NumeroTelephone],
                           contactParCandidats: Option[Boolean],
                           dateInscription: ZonedDateTime,
                           dateDerniereConnexion: ZonedDateTime)
