package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.{Aggregate, AggregateId, Event}

class Candidat(override val id: AggregateId,
               override val version: Int,
               events: List[Event]) extends Aggregate {

  private val state: CandidatState =
    events.foldLeft(CandidatState())((s, e) => s.apply(e))

  // DECIDE
  // TODO : Behavior pour éviter les trop gros aggrégats
  def inscrire(command: InscrireCandidatCommand): List[Event] = {
    if (state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} est déjà inscrit")
    }

    List(CandidatInscrisEvent(
      nom = command.nom,
      prenom = command.prenom,
      email = command.email,
      genre = Some(command.genre.code)
    ))
  }

  def modifierCriteres(command: ModifierCriteresRechercheCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }

    List(CriteresRechercheModifiesEvent(
      rechercheMetierEvalue = command.rechercheMetierEvalue,
      rechercheAutreMetier = command.rechercheAutreMetier,
      listeMetiersRecherches = command.metiersRecherches.map(_.code),
      etreContacteParAgenceInterim = command.etreContacteParAgenceInterim,
      etreContacteParOrganismeFormation = command.etreContacteParOrganismeFormation,
      rayonRecherche = command.rayonRecherche
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
