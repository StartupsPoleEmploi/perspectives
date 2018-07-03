package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.{Aggregate, AggregateId, Event}

class Candidat(override val id: AggregateId,
               override val version: Int,
               events: List[Event]) extends Aggregate {

  private val state: CandidatState =
    events.foldLeft(CandidatState())((s, e) => s.apply(e))

  // DECIDE
  // TODO : Behavior pour éviter les trop gros aggrégats
  def inscrire(inscrireCandidatCommand: InscrireCandidatCommand): List[Event] = {
    if (state.estInscrit) {
      throw new RuntimeException("Le candidat est déjà inscrit")
    }

    List(CandidatInscrisEvent(
      peConnectId = inscrireCandidatCommand.peConnectId,
      nom = inscrireCandidatCommand.nom,
      prenom = inscrireCandidatCommand.prenom,
      email = inscrireCandidatCommand.email
    ))
  }

  def modifierCriteres(modifierCriteresRechercheCommand: ModifierCriteresRechercheCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }

    List(CriteresRechercheModifiesEvent(
      rechercheMetierEvalue = modifierCriteresRechercheCommand.rechercheMetierEvalue,
      rechercheAutreMetier = modifierCriteresRechercheCommand.rechercheAutreMetier,
      listeMetiersRecherches = modifierCriteresRechercheCommand.metiersRecherches,
      etreContacteParAgenceInterim = modifierCriteresRechercheCommand.etreContacteParAgenceInterim,
      etreContacteParOrganismeFormation = modifierCriteresRechercheCommand.etreContacteParOrganismeFormation,
      rayonRecherche = modifierCriteresRechercheCommand.rayonRecherche
    ))
  }
}

// APPLY
private[candidat] case class CandidatState(estInscrit: Boolean = false) {

  def apply(event: Event): CandidatState = event match {
    case e: CandidatInscrisEvent =>
      copy(estInscrit = true)
    case _ => this
  }
}
