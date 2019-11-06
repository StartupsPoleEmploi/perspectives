package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.prospect.domain.ReferentielProspectCandidat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NouveauCandidatState extends CandidatState {

  override def inscrire(context: CandidatContext,
                        command: InscrireCandidatCommand,
                        referentielProspectCandidat: ReferentielProspectCandidat): Future[List[Event]] = {
    referentielProspectCandidat.find(command.email).map(optProspectCandidat =>
      List(CandidatInscritEvent(
        candidatId = command.id,
        peConnectId = optProspectCandidat.map(_.peConnectId),
        identifiantLocal = optProspectCandidat.map(_.identifiantLocal),
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      ))
    )
  }
}
