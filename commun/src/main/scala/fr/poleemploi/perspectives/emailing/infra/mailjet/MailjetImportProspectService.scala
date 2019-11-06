package fr.poleemploi.perspectives.emailing.infra.mailjet

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSDHAEPEConnectAdapter
import fr.poleemploi.perspectives.emailing.domain.{ImportProspectService, MRSProspectCandidat}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportMRSValideeProspectCandidatCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.prospect.domain.{ProspectCandidat, ReferentielProspectCandidat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetImportProspectService(actorSystem: ActorSystem,
                                   importMRSValideeProspectCandidatCSVAdapter: ImportMRSValideeProspectCandidatCSVAdapter,
                                   importMRSDHAEPEConnectAdapter: ImportMRSDHAEPEConnectAdapter,
                                   mailjetSQLAdapter: MailjetSqlAdapter,
                                   mailjetWSAdapter: MailjetWSAdapter,
                                   referentielProspectCandidat: ReferentielProspectCandidat) extends ImportProspectService {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override def importerProspectsCandidats: Future[Stream[MRSProspectCandidat]] =
    for {
      mrsValidees <- importMRSValideeProspectCandidatCSVAdapter.importerProspectsCandidats.map(_.groupBy(_.email))
      mrsDHAEValidees <- importMRSDHAEPEConnectAdapter.importerProspectsCandidats.map(_.groupBy(_.email))
      candidats = mailjetSQLAdapter.streamCandidats
      prospectsCandidatsMRSValidees <- candidats
        .runFold(mrsValidees)(
          (acc, c) => acc - c.email
        ).map(_.values.flatten.toStream)
      prospectsCandidatsMRSDHAEValidees <- candidats
        .runFold(mrsDHAEValidees)(
          (acc, c) => acc - c.email
        ).map(_.values.flatten.toStream)
      prospectsCandidats = prospectsCandidatsMRSValidees ++ prospectsCandidatsMRSDHAEValidees
      _ <- mailjetWSAdapter.importerProspectsCandidats(prospectsCandidats)
      _ <- referentielProspectCandidat.ajouter(prospectsCandidats.map(buildProspectCandidat))
    } yield prospectsCandidats

  private def buildProspectCandidat(x: MRSProspectCandidat) = ProspectCandidat(
    peConnectId = x.peConnectId,
    identifiantLocal = x.identifiantLocal,
    nom = x.nom,
    prenom = x.prenom,
    email = x.email,
    genre = x.genre,
    codeDepartement = x.codeDepartement,
    metier = x.metier,
    dateEvaluation = x.dateEvaluation
  )
}
