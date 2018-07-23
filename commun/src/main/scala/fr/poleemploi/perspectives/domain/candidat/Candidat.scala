package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.domain.{Genre, Metier}

class Candidat(override val id: CandidatId,
               override val version: Int,
               events: List[Event]) extends Aggregate {

  override type Id = CandidatId

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

    if (!state.rechercheMetierEvalue.contains(command.rechercheMetierEvalue) ||
      !state.rechercheAutreMetier.contains(command.rechercheAutreMetier) ||
      !state.metiersRecherches.forall(command.metiersRecherches.contains) ||
      !command.metiersRecherches.forall(state.metiersRecherches.contains) ||
      !state.etreContacteParAgenceInterim.contains(command.etreContacteParAgenceInterim) ||
      !state.etreContacteParOrganismeFormation.contains(command.etreContacteParOrganismeFormation) ||
      !state.rayonRecherche.contains(command.rayonRecherche)) {
      List(CriteresRechercheModifiesEvent(
        rechercheMetierEvalue = command.rechercheMetierEvalue,
        rechercheAutreMetier = command.rechercheAutreMetier,
        listeMetiersRecherches = command.metiersRecherches.map(_.code),
        etreContacteParAgenceInterim = command.etreContacteParAgenceInterim,
        etreContacteParOrganismeFormation = command.etreContacteParOrganismeFormation,
        rayonRecherche = command.rayonRecherche
      ))
    } else Nil
  }

  def modifierProfilPEConnect(command: ModifierProfilPEConnectCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }

    if (!state.nom.contains(command.nom) ||
      !state.prenom.contains(command.prenom) ||
      !state.email.contains(command.email) ||
      !state.genre.contains(command.genre)) {
      List(ProfilCandidatModifiePEConnectEvent(
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre.code
      ))
    } else Nil
  }
}

// APPLY
private[candidat] case class CandidatState(estInscrit: Boolean = false,
                                           nom: Option[String] = None,
                                           prenom: Option[String] = None,
                                           email: Option[String] = None,
                                           genre: Option[Genre] = None,
                                           rechercheMetierEvalue: Option[Boolean] = None,
                                           rechercheAutreMetier: Option[Boolean] = None,
                                           metiersRecherches: Set[Metier] = Set.empty,
                                           etreContacteParAgenceInterim: Option[Boolean] = None,
                                           etreContacteParOrganismeFormation: Option[Boolean] = None,
                                           rayonRecherche: Option[Int] = None) {

  def apply(event: Event): CandidatState = event match {
    case e: CandidatInscrisEvent =>
      copy(
        estInscrit = true,
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = e.genre.flatMap(Genre.from)
      )
    case e: ProfilCandidatModifiePEConnectEvent =>
      copy(
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Genre.from(e.genre)
      )
    case e: CriteresRechercheModifiesEvent =>
      copy(
        rechercheMetierEvalue = Some(e.rechercheMetierEvalue),
        rechercheAutreMetier = Some(e.rechercheAutreMetier),
        metiersRecherches = e.listeMetiersRecherches.flatMap(Metier.from),
        etreContacteParAgenceInterim = Some(e.etreContacteParAgenceInterim),
        etreContacteParOrganismeFormation = Some(e.etreContacteParOrganismeFormation),
        rayonRecherche = Some(e.rayonRecherche)
      )
    case _ => this
  }
}
