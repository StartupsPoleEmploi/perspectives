package fr.poleemploi.perspectives.candidat.state
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NouveauCandidatState extends CandidatState {

  override def name: String = "Nouveau"

  override def inscrire(context: CandidatContext,
                        command: InscrireCandidatCommand,
                        localisationService: LocalisationService): Future[List[Event]]  = {
    val candidatInscritEvent = Future.successful(Some(
      CandidatInscritEvent(
        candidatId = command.id,
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      )
    ))
    val adresseModifieeEvent = command.adresse.map(adresse =>
      localisationService.localiser(adresse).map(optCoordonnees => Some(
        AdresseModifieeEvent(
          candidatId = command.id,
          adresse = adresse,
          coordonnees = optCoordonnees
        )
      )).recover {
        case _: Throwable => Some(
          AdresseModifieeEvent(
            candidatId = command.id,
            adresse = adresse,
            coordonnees = None
          )
        )
      }
    ).getOrElse(Future.successful(None))

    val statutDemandeurEmploiModifieEvent = Future.successful(command.statutDemandeurEmploi.map(statutDemandeurEmploi =>
      StatutDemandeurEmploiModifieEvent(
        candidatId = command.id,
        statutDemandeurEmploi = statutDemandeurEmploi
      )
    ))

    Future.sequence(List(candidatInscritEvent, adresseModifieeEvent, statutDemandeurEmploiModifieEvent))
      .map(_.flatten)
  }
}
