package fr.poleemploi.perspectives.projections.conseiller

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult}
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import fr.poleemploi.perspectives.projections.conseiller.mrs.{CodeROMEParDepartementQuery, CodeROMEParDepartementQueryResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConseillerQueryHandler(referentielHabiletesMRS: ReferentielHabiletesMRS) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case CodeROMEParDepartementQuery => referentielHabiletesMRS.codeROMEsParDepartement.map(CodeROMEParDepartementQueryResult(_))
  }
}
