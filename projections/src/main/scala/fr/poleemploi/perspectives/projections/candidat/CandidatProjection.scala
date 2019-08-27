package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._

import scala.concurrent.Future

trait CandidatProjection extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
    case e: CandidatConnecteEvent => onCandidatConnecteEvent(e)
    case e: CandidatAutologgeEvent => onCandidatAutologgeEvent(e)
    case e: ProfilCandidatModifieEvent => onProfilModifieEvent(e)
    case e: VisibiliteRecruteurModifieeEvent => onVisibiliteRecruteurModifieeEvent(e)
    case e: CriteresRechercheModifiesEvent => onCriteresRechercheModifiesEvent(e)
    case e: DisponibilitesModifieesEvent => onDisponibilitesModifieesEvent(e)
    case e: NumeroTelephoneModifieEvent => onNumeroTelephoneModifieEvent(e)
    case e: AdresseModifieeEvent => onAdresseModifieeEvent(e)
    case e: StatutDemandeurEmploiModifieEvent => onStatutDemandeurEmploiModifieEvent(e)
    case e: CentresInteretModifiesEvent => onCentresInteretModifiesEvent(e)
    case e: LanguesModifieesEvent => onLanguesModifieesEvent(e)
    case e: PermisModifiesEvent => onPermisModifiesEvent(e)
    case e: SavoirEtreModifiesEvent => onSavoirEtreModifiesEvent(e)
    case e: SavoirFaireModifiesEvent => onSavoirFaireModifiesEvent(e)
    case e: FormationsModifieesEvent => onFormationsModifieesEvent(e)
    case e: ExperiencesProfessionnellesModifieesEvent => onExperiencesProfessionnellesModifieesEvent(e)
    case e: CVAjouteEvent => onCVAjouteEvent(e)
    case e: CVRemplaceEvent => onCVRemplaceEvent(e)
    case e: MRSAjouteeEvent => onMRSAjouteeEvent(e)
    case e: RepriseEmploiDeclareeParConseillerEvent => onRepriseEmploiDeclareeParConseillerEvent(e)
  }

  def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit]

  def onVisibiliteRecruteurModifieeEvent(event: VisibiliteRecruteurModifieeEvent): Future[Unit]

  def onCandidatConnecteEvent(event: CandidatConnecteEvent): Future[Unit]

  def onCandidatAutologgeEvent(event: CandidatAutologgeEvent): Future[Unit]

  def onProfilModifieEvent(event: ProfilCandidatModifieEvent): Future[Unit]

  def onCriteresRechercheModifiesEvent(event: CriteresRechercheModifiesEvent): Future[Unit]

  def onDisponibilitesModifieesEvent(event: DisponibilitesModifieesEvent): Future[Unit]

  def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit]

  def onStatutDemandeurEmploiModifieEvent(event: StatutDemandeurEmploiModifieEvent): Future[Unit]

  def onCentresInteretModifiesEvent(event: CentresInteretModifiesEvent): Future[Unit]

  def onLanguesModifieesEvent(event: LanguesModifieesEvent): Future[Unit]

  def onPermisModifiesEvent(event: PermisModifiesEvent): Future[Unit]

  def onSavoirEtreModifiesEvent(event: SavoirEtreModifiesEvent): Future[Unit]

  def onSavoirFaireModifiesEvent(event: SavoirFaireModifiesEvent): Future[Unit]

  def onFormationsModifieesEvent(event: FormationsModifieesEvent): Future[Unit]

  def onExperiencesProfessionnellesModifieesEvent(event: ExperiencesProfessionnellesModifieesEvent): Future[Unit]

  def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit]

  def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit]

  def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit]

  def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit]

  def onRepriseEmploiDeclareeParConseillerEvent(event: RepriseEmploiDeclareeParConseillerEvent): Future[Unit]
}
