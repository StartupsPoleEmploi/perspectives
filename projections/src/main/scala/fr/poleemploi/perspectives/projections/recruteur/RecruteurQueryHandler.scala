package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult}
import fr.poleemploi.perspectives.rome.domain.ReferentielRome

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjectionQuery: RecruteurProjectionQuery,
                            referentielRome: ReferentielRome) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case q: TypeRecruteurQuery => recruteurProjectionQuery.typeRecruteur(q)
    case q: ProfilRecruteurQuery => recruteurProjectionQuery.profilRecruteur(q)
    case q: RecruteursPourConseillerQuery => recruteurProjectionQuery.listerPourConseiller(q)
    case q: MetiersRecruteurQuery => referentielRome.appellationsRecherche(q.query)
      .map(MetiersRecruteurQueryResult(_))
  }
}
