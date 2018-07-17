package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.{Aggregate, AggregateId, Event}
import fr.poleemploi.perspectives.domain.NumeroTelephone

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

    if (!state.raisonSociale.contains(command.raisonSociale) ||
      !state.numeroSiret.contains(command.numeroSiret) ||
      !state.typeRecruteur.contains(command.typeRecruteur) ||
      !state.contactParCandidats.contains(command.contactParCandidats) ||
      !state.numeroTelephone.contains(command.numeroTelephone)) {
      List(ProfilModifieEvent(
        raisonSociale = command.raisonSociale,
        numeroSiret = command.numeroSiret.value,
        typeRecruteur = command.typeRecruteur.code,
        contactParCandidats = command.contactParCandidats,
        numeroTelephone = command.numeroTelephone.value
      ))
    } else Nil
  }
}

private[recruteur] case class RecruteurState(estInscrit: Boolean = false,
                                             raisonSociale: Option[String] = None,
                                             numeroSiret: Option[NumeroSiret] = None,
                                             typeRecruteur: Option[TypeRecruteur] = None,
                                             contactParCandidats: Option[Boolean] = None,
                                             numeroTelephone: Option[NumeroTelephone] = None) {

  def apply(event: Event): RecruteurState = event match {
    case _: RecruteurInscrisEvent =>
      copy(estInscrit = true)
    case e: ProfilModifieEvent =>
      copy(
        raisonSociale = Some(e.raisonSociale),
        numeroSiret = NumeroSiret.from(e.numeroSiret),
        typeRecruteur = TypeRecruteur.from(e.typeRecruteur),
        contactParCandidats = Some(e.contactParCandidats),
        numeroTelephone = NumeroTelephone.from(e.numeroTelephone)
      )
    case _ => this
  }
}
