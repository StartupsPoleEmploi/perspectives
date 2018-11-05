package fr.poleemploi.perspectives.candidat.state
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._

object NouveauCandidatState extends CandidatState {

  override def name: String = "Nouveau"

  override def inscrire(context: CandidatContext, command: InscrireCandidatCommand): List[Event] = {
    val candidatInscritEvent = Some(
      CandidatInscritEvent(
        candidatId = command.id,
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      )
    )
    val adresseModifieeEvent = command.adresse.map(adresse =>
      AdresseModifieeEvent(
        candidatId = command.id,
        adresse = adresse
      ))
    val statutDemandeurEmploiModifieEvent = command.statutDemandeurEmploi.map(statutDemandeurEmploi =>
      StatutDemandeurEmploiModifieEvent(
        candidatId = command.id,
        statutDemandeurEmploi = statutDemandeurEmploi
      )
    )

    List(candidatInscritEvent, adresseModifieeEvent, statutDemandeurEmploiModifieEvent).flatten
  }
}
