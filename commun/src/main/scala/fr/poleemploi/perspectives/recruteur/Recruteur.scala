package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.state.{NouveauRecruteurState, RecruteurInscritState, RecruteurProfilCompletState, RecruteurState}

case class Recruteur(id: RecruteurId,
                     version: Int,
                     state: RecruteurContext) extends Aggregate {

  override type Id = RecruteurId

  def inscrire(command: InscrireRecruteurCommand): List[Event] =
    behavior.inscrire(context = state, command = command)

  def modifierProfil(command: ModifierProfilCommand): List[Event] =
    behavior.modifierProfil(context = state, command = command)

  def connecter(command: ConnecterRecruteurCommand): List[Event] =
    behavior.connecter(context = state, command = command)

  private def behavior: RecruteurState = state.statut match {
    case StatutRecruteur.NOUVEAU => NouveauRecruteurState
    case StatutRecruteur.INSCRIT => RecruteurInscritState
    case StatutRecruteur.PROFIL_COMPLET => RecruteurProfilCompletState
    case s@_ => throw new IllegalArgumentException(s"Etat du recruteur non valide : $s")
  }
}

private[recruteur] case class RecruteurContext(statut: StatutRecruteur = StatutRecruteur.NOUVEAU,
                                               nom: Option[Nom] = None,
                                               prenom: Option[Prenom] = None,
                                               email: Option[Email] = None,
                                               genre: Option[Genre] = None,
                                               raisonSociale: Option[String] = None,
                                               numeroSiret: Option[NumeroSiret] = None,
                                               typeRecruteur: Option[TypeRecruteur] = None,
                                               contactParCandidats: Option[Boolean] = None,
                                               numeroTelephone: Option[NumeroTelephone] = None) {

  def apply(events: List[Event]): RecruteurContext =
    events.foldLeft(this)((context, event) => event match {
      case e: RecruteurInscritEvent =>
        context.copy(
          statut = StatutRecruteur.INSCRIT,
          nom = Some(e.nom),
          prenom = Some(e.prenom),
          email = Some(e.email),
          genre = Some(e.genre)
        )
      case e: ProfilModifieEvent =>
        context.copy(
          statut = StatutRecruteur.PROFIL_COMPLET,
          raisonSociale = Some(e.raisonSociale),
          numeroSiret = Some(e.numeroSiret),
          typeRecruteur = Some(e.typeRecruteur),
          contactParCandidats = Some(e.contactParCandidats),
          numeroTelephone = Some(e.numeroTelephone)
        )
      case e: ProfilGerantModifieEvent =>
        context.copy(
          nom = Some(e.nom),
          prenom = Some(e.prenom),
          email = Some(e.email),
          genre = Some(e.genre)
        )
      case _ => context
    })

}