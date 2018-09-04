package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.QueryHandler

import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjection: RecruteurProjection) extends QueryHandler {

  def profilRecruteur(query: ProfilRecruteurQuery): Future[ProfilRecruteurDto] =
    recruteurProjection.profilRecruteur(query)

  def listerParDateInscriptionPourConseiller: Future[List[RecruteurPourConseillerDto]] =
    recruteurProjection.listerParDateInscriptionPourConseiller
}
