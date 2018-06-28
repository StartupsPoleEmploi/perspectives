package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.{Aggregate, AggregateId, Event}

class Recruteur(override val id: AggregateId,
                override val version: Int,
                events: List[Event]) extends Aggregate {

  private val state: RecruteurState =
    events.foldLeft(RecruteurState())((s, e) => s.apply(e))

  def inscrire(command: InscrireRecruteurCommand): List[Event] = {
    if (state.estInscrit) {
      throw new RuntimeException(s"Le recruteur ${id.value} est déjà inscrit")
    }

    List(RecruteurInscrisEvent(
      nom = command.nom,
      prenom = command.prenom,
      email = command.email,
      genre = command.genre.code
    ))
  }

  def modifierProfil(command: ModifierProfilCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }

    List(ProfilModifieEvent(
      raisonSociale = command.raisonSociale,
      numeroSiret = command.numeroSiret.value,
      typeRecruteur = command.typeRecruteur.code,
      contactParCandidats = command.contactParCandidats,
      numeroTelephone = command.numeroTelephone.value
    ))
  }
}

private[recruteur] case class RecruteurState(estInscrit: Boolean = false) {

  def apply(event: Event): RecruteurState = event match {
    case e: RecruteurInscrisEvent =>
      copy(estInscrit = true)
    case _ => this
  }
}
