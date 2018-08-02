package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.domain.candidat.cv.{CVId, CVService}
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

    List(
      CandidatInscrisEvent(
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = Some(command.genre)
      ),
      AdressePEConnectModifieeEvent(
        adresse = command.adresse
      ),
      StatutDemandeurEmploiPEConnectModifieEvent(
        statutDemandeurEmploi = command.statutDemandeurEmploi
      )
    )
  }

  def modifierCriteres(command: ModifierCriteresRechercheCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }

    val criteresRechercheModifiesEvent =
      if (!state.rechercheMetierEvalue.contains(command.rechercheMetierEvalue) ||
      !state.rechercheAutreMetier.contains(command.rechercheAutreMetier) ||
      !state.metiersRecherches.forall(command.metiersRecherches.contains) ||
      !command.metiersRecherches.forall(state.metiersRecherches.contains) ||
      !state.etreContacteParAgenceInterim.contains(command.etreContacteParAgenceInterim) ||
      !state.etreContacteParOrganismeFormation.contains(command.etreContacteParOrganismeFormation) ||
      !state.rayonRecherche.contains(command.rayonRecherche)) {
      Some(CriteresRechercheModifiesEvent(
        rechercheMetierEvalue = command.rechercheMetierEvalue,
        rechercheAutreMetier = command.rechercheAutreMetier,
        metiersRecherches = command.metiersRecherches,
        etreContacteParAgenceInterim = command.etreContacteParAgenceInterim,
        etreContacteParOrganismeFormation = command.etreContacteParOrganismeFormation,
        rayonRecherche = command.rayonRecherche
      ))
    } else None

    val numeroTelephoneModifieEvent =
      if (!state.numeroTelephone.contains(command.numeroTelephone)) {
      Some(NumeroTelephoneModifieEvent(
        numeroTelephone = command.numeroTelephone
      ))
    } else None

    List(criteresRechercheModifiesEvent, numeroTelephoneModifieEvent).flatten
  }

  def modifierProfilPEConnect(command: ModifierProfilPEConnectCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }

    val profilCandidatModifiePEConnectEvent =
      if (!state.nom.contains(command.nom) ||
      !state.prenom.contains(command.prenom) ||
      !state.email.contains(command.email) ||
      !state.genre.contains(command.genre)) {
      Some(ProfilCandidatModifiePEConnectEvent(
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      ))
    } else None

    val adressePEConnectModifieeEvent =
      if (!state.adresse.contains(command.adresse)) {
      Some(AdressePEConnectModifieeEvent(
        adresse = command.adresse
      ))
    } else None

    val statutDemandeurEmploiPEConnectModifieEvent =
      if (!state.statutDemandeurEmploi.contains(command.statutDemandeurEmploi)) {
        Some(StatutDemandeurEmploiPEConnectModifieEvent(
          statutDemandeurEmploi = command.statutDemandeurEmploi
        ))
      } else None

    List(profilCandidatModifiePEConnectEvent, adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent).flatten
  }

  def ajouterCV(command: AjouterCVCommand,
                cvService: CVService): Future[List[Event]] = {
    if (!state.estInscrit) {
      Future.failed(throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit"))
    }

    cvService.save(
      cvId = CVId(UUID.randomUUID().toString),
      candidatId = command.id,
      nomFichier = command.nomFichier,
      typeMedia = command.typeMedia,
      path = command.path
    ).map(_ => Nil)
  }

  def remplacerCV(command: RemplacerCVCommand,
                  cvService: CVService): Future[List[Event]] = {
    if (!state.estInscrit) {
      Future.failed(throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit"))
    }

    cvService.update(
      cvId = command.cvId,
      nomFichier = command.nomFichier,
      typeMedia = command.typeMedia,
      path = command.path
    ).map(_ => Nil)
  }
}

// APPLY
private[candidat] case class CandidatState(estInscrit: Boolean = false,
                                           nom: Option[String] = None,
                                           prenom: Option[String] = None,
                                           email: Option[String] = None,
                                           genre: Option[Genre] = None,
                                           adresse: Option[Adresse] = None,
                                           statutDemandeurEmploi: Option[StatutDemandeurEmploi] = None,
                                           rechercheMetierEvalue: Option[Boolean] = None,
                                           rechercheAutreMetier: Option[Boolean] = None,
                                           metiersRecherches: Set[Metier] = Set.empty,
                                           etreContacteParAgenceInterim: Option[Boolean] = None,
                                           etreContacteParOrganismeFormation: Option[Boolean] = None,
                                           rayonRecherche: Option[RayonRecherche] = None,
                                           numeroTelephone: Option[NumeroTelephone] = None,
                                           cvId: Option[CVId] = None) {

  def apply(event: Event): CandidatState = event match {
    case e: CandidatInscrisEvent =>
      copy(
        estInscrit = true,
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = e.genre
      )
    case e: ProfilCandidatModifiePEConnectEvent =>
      copy(
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Some(e.genre)
      )
    case e: CriteresRechercheModifiesEvent =>
      copy(
        rechercheMetierEvalue = Some(e.rechercheMetierEvalue),
        rechercheAutreMetier = Some(e.rechercheAutreMetier),
        metiersRecherches = e.metiersRecherches,
        etreContacteParAgenceInterim = Some(e.etreContacteParAgenceInterim),
        etreContacteParOrganismeFormation = Some(e.etreContacteParOrganismeFormation),
        rayonRecherche = Some(e.rayonRecherche)
      )
    case e: NumeroTelephoneModifieEvent =>
      copy(numeroTelephone = Some(e.numeroTelephone))
    case e: AdressePEConnectModifieeEvent =>
      copy(adresse = Some(e.adresse))
    case e: StatutDemandeurEmploiPEConnectModifieEvent =>
      copy(statutDemandeurEmploi = Some(e.statutDemandeurEmploi))
    case _ => this
  }
}
