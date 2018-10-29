package fr.poleemploi.perspectives.projections.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}

case class RecruteursPourConseillerQuery(nbRecruteursParPage: Int,
                                         nbPagesACharger: Int,
                                         avantDateInscription: ZonedDateTime) extends Query[RecruteursPourConseillerQueryResult]

case class RecruteursPourConseillerQueryResult(recruteurs: List[RecruteurPourConseillerDto],
                                               pages: List[ZonedDateTime],
                                               derniereDateInscription: Option[ZonedDateTime]) extends QueryResult

case class RecruteurPourConseillerDto(recruteurId: RecruteurId,
                                      nom: String,
                                      prenom: String,
                                      email: Email,
                                      genre: Genre,
                                      typeRecruteur: Option[TypeRecruteur],
                                      raisonSociale: Option[String],
                                      numeroSiret: Option[NumeroSiret],
                                      numeroTelephone: Option[NumeroTelephone],
                                      contactParCandidats: Option[Boolean],
                                      dateInscription: ZonedDateTime,
                                      dateDerniereConnexion: ZonedDateTime)
