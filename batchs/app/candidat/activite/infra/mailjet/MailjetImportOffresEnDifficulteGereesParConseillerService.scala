package candidat.activite.infra.mailjet

import akka.actor.ActorSystem
import candidat.activite.domain.{ImportOffresEnDifficulteGereesParConseillerService, OffresGereesParConseillerImportService}
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.{CodeSafir, Email}
import fr.poleemploi.perspectives.emailing.domain.{OffreGereeParConseiller, OffreGereeParConseillerAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportOffresEnDifficulteGereesParConseillerCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler

import scala.concurrent.Future

class MailjetImportOffresEnDifficulteGereesParConseillerService(override val actorSystem: ActorSystem,
                                                                override val baseUrl: String,
                                                                override val correspondantsOffresParCodeSafir: Map[CodeSafir, Seq[Email]],
                                                                override val localisationService: LocalisationService,
                                                                override val candidatQueryHandler: CandidatQueryHandler,
                                                                override val mailjetWSAdapter: MailjetWSAdapter,
                                                                importOffresEnDifficulteGereesParConseillerCSVAdapter: ImportOffresEnDifficulteGereesParConseillerCSVAdapter)
  extends ImportOffresEnDifficulteGereesParConseillerService with OffresGereesParConseillerImportService {

  override def importerOffresEnDifficulteGereesParConseiller: Future[Stream[OffreGereeParConseillerAvecCandidats]] =
    importerOffresGereesParConseiller

  override def importerOffres: Future[Stream[OffreGereeParConseiller]] =
    importOffresEnDifficulteGereesParConseillerCSVAdapter.importerOffres

  override def envoyerCandidatsParMailPourOffreGereeParConseiller(offresGereesParConseillerAvecCandidats: Seq[OffreGereeParConseillerAvecCandidats]): Future[Unit] =
    mailjetWSAdapter.envoyerCandidatsPourOffreEnDifficulteGereeParConseiller(baseUrl, correspondantsOffresParCodeSafir, offresGereesParConseillerAvecCandidats)

}
