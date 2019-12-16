package fr.poleemploi.perspectives.projections.candidat

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.metier.domain.Metier

case object CandidatsPourCsvQuery extends Query[CandidatsPourCsvQueryResult]

case class CandidatsPourCsvQueryResult(source: Source[CandidatPourCsvDto, NotUsed]) extends QueryResult

case class CandidatPourCsvDto(peConnectId: Option[PEConnectId],
                              identifiantLocal: Option[IdentifiantLocal],
                              codeNeptune: Option[CodeNeptune],
                              nom: Nom,
                              prenom: Prenom,
                              email: Email,
                              genre: Genre,
                              codeDepartement: CodeDepartement,
                              metier: Metier)
