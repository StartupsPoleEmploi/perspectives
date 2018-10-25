package fr.poleemploi.perspectives.projections.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.recruteur.RecruteurId

sealed trait RecruteurQuery extends Query

case class ProfilRecruteurQuery(recruteurId: RecruteurId) extends RecruteurQuery

case class RecruteursPourConseillerQuery(nbRecruteursParPage: Int,
                                         nbPagesACharger: Int,
                                         avantDateInscription: ZonedDateTime) extends RecruteurQuery

case class RecruteursPourConseillerQueryResult(recruteurs: List[RecruteurPourConseillerDto],
                                               pages: List[ZonedDateTime],
                                               derniereDateInscription: Option[ZonedDateTime])

case class AlertesRecruteurQuery(recruteurId: RecruteurId) extends RecruteurQuery