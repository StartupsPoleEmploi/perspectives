package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, CVService}
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielHabiletesMRS}
import fr.poleemploi.perspectives.candidat.state.{CandidatInscritState, CandidatState, NouveauCandidatState}
import fr.poleemploi.perspectives.commun.domain._

import scala.concurrent.Future

class Candidat(override val id: CandidatId,
               override val version: Int,
               events: List[Event]) extends Aggregate {

  override type Id = CandidatId

  private val state: CandidatState =
    events.foldLeft[CandidatState](NouveauCandidatState)((state, event) => event match {
      case _: CandidatInscritEvent => CandidatInscritState
      case _ => state
    })

  private val context: CandidatContext =
    events.foldLeft(CandidatContext())((context, event) => event match {
      case e: CandidatInscritEvent =>
        context.copy(
          nom = Some(e.nom),
          prenom = Some(e.prenom),
          email = Some(e.email),
          genre = Some(e.genre),
          // Par défaut un candidat qui s'inscrit est disponible tant qu'il n'a pas renseigné ces critères, pour qu'on puisse déclarer sa reprise d'emploi s'il a été suivi manuellement
          rechercheEmploi = Some(true)
        )
      case e: ProfilCandidatModifieEvent =>
        context.copy(
          nom = Some(e.nom),
          prenom = Some(e.prenom),
          email = Some(e.email),
          genre = Some(e.genre)
        )
      case e: VisibiliteRecruteurModifieeEvent =>
        context.copy(
          contactRecruteur = Some(e.contactRecruteur),
          contactFormation = Some(e.contactFormation),
          rechercheEmploi = Some(e.contactRecruteur || e.contactFormation)
        )
      case e: CriteresRechercheModifiesEvent =>
        context.copy(
          codesROMEValidesRecherches = e.codesROMEValidesRecherches,
          codesROMERecherches = e.codesROMERecherches,
          codesDomaineProfessionnelRecherches = e.codesDomaineProfessionnelRecherches,
          localisationRecherche = Some(e.localisationRecherche)
        )
      case e: NumeroTelephoneModifieEvent =>
        context.copy(numeroTelephone = Some(e.numeroTelephone))
      case e: AdresseModifieeEvent =>
        context.copy(
          adresse = Some(e.adresse),
          coordonnees = Some(e.coordonnees)
        )
      case e: StatutDemandeurEmploiModifieEvent =>
        context.copy(statutDemandeurEmploi = Some(e.statutDemandeurEmploi))
      case e: CVAjouteEvent =>
        context.copy(cvId = Some(e.cvId))
      case e: CVRemplaceEvent =>
        context.copy(cvId = Some(e.cvId))
      case e: MRSAjouteeEvent =>
        context.copy(
          mrsValidees = MRSValidee(
            codeROME = e.codeROME,
            codeDepartement = e.departement,
            dateEvaluation = e.dateEvaluation) :: context.mrsValidees
        )
      case _: RepriseEmploiDeclareeParConseillerEvent =>
        context.copy(rechercheEmploi = Some(false))
      case _ => context
    })

  def inscrire(command: InscrireCandidatCommand, localisationService: LocalisationService): Future[List[Event]] =
    state.inscrire(context = context, command = command, localisationService = localisationService)

  def modifierCandidat(command: ModifierCandidatCommand): List[Event] =
    state.modifierCandidat(context = context, command = command)

  def connecter(command: ConnecterCandidatCommand, localisationService: LocalisationService): Future[List[Event]] =
    state.connecter(context = context, command = command, localisationService = localisationService)

  def ajouterCV(command: AjouterCVCommand, cvService: CVService): Future[List[Event]] =
    state.ajouterCV(context = context, command = command, cvService = cvService)

  def remplacerCV(command: RemplacerCVCommand, cvService: CVService): Future[List[Event]] =
    state.remplacerCV(context = context, command = command, cvService = cvService)

  def ajouterMRSValidee(command: AjouterMRSValideesCommand, referentielHabiletesMRS: ReferentielHabiletesMRS): Future[List[Event]] =
    state.ajouterMRSValidee(context = context, command = command, referentielHabiletesMRS = referentielHabiletesMRS)

  def declarerRepriseEmploiParConseiller(command: DeclarerRepriseEmploiParConseillerCommand): List[Event] =
    state.declarerRepriseEmploiParConseiller(context = context, command = command)
}

private[candidat] case class CandidatContext(nom: Option[Nom] = None,
                                             prenom: Option[Prenom] = None,
                                             email: Option[Email] = None,
                                             genre: Option[Genre] = None,
                                             adresse: Option[Adresse] = None,
                                             coordonnees: Option[Coordonnees] = None,
                                             statutDemandeurEmploi: Option[StatutDemandeurEmploi] = None,
                                             contactRecruteur: Option[Boolean] = None,
                                             contactFormation: Option[Boolean] = None,
                                             mrsValidees: List[MRSValidee] = Nil,
                                             codesROMEValidesRecherches: Set[CodeROME] = Set.empty,
                                             codesROMERecherches: Set[CodeROME] = Set.empty,
                                             codesDomaineProfessionnelRecherches: Set[CodeDomaineProfessionnel] = Set.empty,
                                             localisationRecherche: Option[LocalisationRecherche] = None,
                                             numeroTelephone: Option[NumeroTelephone] = None,
                                             cvId: Option[CVId] = None,
                                             rechercheEmploi: Option[Boolean] = None)