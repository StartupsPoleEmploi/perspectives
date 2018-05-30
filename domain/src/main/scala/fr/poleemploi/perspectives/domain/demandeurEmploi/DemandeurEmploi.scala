package fr.poleemploi.perspectives.domain.demandeurEmploi

import fr.poleemploi.eventsourcing.{Aggregate, AggregateId, Event}

class DemandeurEmploi(override val id: AggregateId,
                      override val version: Int,
                      events: List[Event]) extends Aggregate {

  private val state: DemandeurEmploiState =
    events.foldLeft(DemandeurEmploiState())((s, e) => s.apply(e))

  /**
    * Consider Using Two Implementation Classes to make your code clearer,
    * you can split the A+ES implementation into two distinct classes, one for state and one for behavior,
    * with the state object being held by the behavioral.
    * The two objects would collaborate exclusively through the Apply() method.
    * This ensures that state is mutated only by means of Events
    */
  // DECIDE
  // TODO : Behavior pour éviter les trop gros aggrégats
  def inscrire(): List[Event] = {
    if (state.estInscrit) {
      throw new RuntimeException("L'utilisateur est déjà inscrit")
    }
    List(DemandeurEmploiInscrisEvent())
  }
}

// APPLY
private[demandeurEmploi] case class DemandeurEmploiState(estInscrit: Boolean = false) {

  def apply(event: Event): DemandeurEmploiState = event match {
    case e: DemandeurEmploiInscrisEvent =>
      copy(estInscrit = true)
    case _ => this
  }
}
