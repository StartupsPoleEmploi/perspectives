package candidat.activite.infra.mailjet

import akka.actor.ActorSystem
import candidat.activite.domain.{ImportOffresGereesParRecruteurService, OffresGereesParRecruteurImportService}
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.emailing.domain.{OffreGereeParRecruteur, OffreGereeParRecruteurAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportOffresGereesParRecruteurCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler

import scala.concurrent.Future

class MailjetImportOffresGereesParRecruteurService(override val actorSystem: ActorSystem,
                                                   override val baseUrl: String,
                                                   importOffresGereesParRecruteurCSVAdapter: ImportOffresGereesParRecruteurCSVAdapter,
                                                   override val localisationService: LocalisationService,
                                                   override val candidatQueryHandler: CandidatQueryHandler,
                                                   override val mailjetWSAdapter: MailjetWSAdapter)
  extends ImportOffresGereesParRecruteurService with OffresGereesParRecruteurImportService {

  override def importerOffres: Future[Stream[OffreGereeParRecruteur]] =
    importOffresGereesParRecruteurCSVAdapter.importerOffres

  override def envoyerCandidatsParMailPourOffreGereeParRecruteur(offresGereesParRecruteurAvecCandidats: Seq[OffreGereeParRecruteurAvecCandidats]): Future[Unit] =
    mailjetWSAdapter.envoyerCandidatsPourOffreGereeParRecruteur(baseUrl, offresGereesParRecruteurAvecCandidats)

}
