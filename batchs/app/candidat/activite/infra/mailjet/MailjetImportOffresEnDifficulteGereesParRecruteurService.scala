package candidat.activite.infra.mailjet

import akka.actor.ActorSystem
import candidat.activite.domain.{ImportOffresEnDifficulteGereesParRecruteurService, OffresGereesParRecruteurImportService}
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.{CodeSafir, Email}
import fr.poleemploi.perspectives.emailing.domain.{OffreGereeParRecruteur, OffreGereeParRecruteurAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportOffresEnDifficulteGereesParRecruteurCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler

import scala.concurrent.Future

class MailjetImportOffresEnDifficulteGereesParRecruteurService(override val actorSystem: ActorSystem,
                                                               override val baseUrl: String,
                                                               override val correspondantsOffresParCodeSafir: Map[CodeSafir, Seq[Email]],
                                                               importOffresEnDifficulteGereesParRecruteurCSVAdapter: ImportOffresEnDifficulteGereesParRecruteurCSVAdapter,
                                                               override val localisationService: LocalisationService,
                                                               override val candidatQueryHandler: CandidatQueryHandler,
                                                               override val mailjetWSAdapter: MailjetWSAdapter)
  extends ImportOffresEnDifficulteGereesParRecruteurService with OffresGereesParRecruteurImportService {

  override def importerOffresEnDifficulteGereesParRecruteur: Future[Stream[OffreGereeParRecruteurAvecCandidats]] =
    importerOffresGereesParRecruteur

  override def importerOffres: Future[Stream[OffreGereeParRecruteur]] =
    importOffresEnDifficulteGereesParRecruteurCSVAdapter.importerOffres

  override def envoyerCandidatsParMailPourOffreGereeParRecruteur(offresGereesParRecruteurAvecCandidats: Seq[OffreGereeParRecruteurAvecCandidats]): Future[Unit] =
    mailjetWSAdapter.envoyerCandidatsPourOffreEnDifficulteGereeParRecruteur(baseUrl, correspondantsOffresParCodeSafir, offresGereesParRecruteurAvecCandidats)

}
