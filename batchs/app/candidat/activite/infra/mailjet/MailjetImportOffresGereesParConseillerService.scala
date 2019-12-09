package candidat.activite.infra.mailjet

import akka.actor.ActorSystem
import candidat.activite.domain.{OffresGereesParConseillerImportService, ImportOffresGereesParConseillerService}
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.emailing.domain.{OffreGereeParConseiller, OffreGereeParConseillerAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportOffresGereesParConseillerCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler

import scala.concurrent.Future

class MailjetImportOffresGereesParConseillerService(override val actorSystem: ActorSystem,
                                                    override val baseUrl: String,
                                                    override val localisationService: LocalisationService,
                                                    override val candidatQueryHandler: CandidatQueryHandler,
                                                    override val mailjetWSAdapter: MailjetWSAdapter,
                                                    importOffresGereesParConseillerCSVAdapter: ImportOffresGereesParConseillerCSVAdapter)
  extends ImportOffresGereesParConseillerService with OffresGereesParConseillerImportService {

  override def importerOffres: Future[Stream[OffreGereeParConseiller]] =
    importOffresGereesParConseillerCSVAdapter.importerOffres

  override def envoyerCandidatsParMailPourOffreGereeParConseiller(offresGereesParConseillerAvecCandidats: Seq[OffreGereeParConseillerAvecCandidats]): Future[Unit] =
    mailjetWSAdapter.envoyerCandidatsPourOffreGereeParConseiller(baseUrl, offresGereesParConseillerAvecCandidats)

}
