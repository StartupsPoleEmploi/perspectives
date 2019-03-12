package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult}
import fr.poleemploi.perspectives.projections.recruteur.alerte.{AlerteRecruteurProjection, AlertesRecruteurQuery}

import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjection: RecruteurProjection,
                            alerteRecruteurProjection: AlerteRecruteurProjection) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case q: TypeRecruteurQuery => recruteurProjection.typeRecruteur(q)
    case q: ProfilRecruteurQuery => recruteurProjection.profilRecruteur(q)
    case q: RecruteursPourConseillerQuery => recruteurProjection.listerPourConseiller(q)
    case q: AlertesRecruteurQuery => alerteRecruteurProjection.alertesParRecruteur(q)
  }
}