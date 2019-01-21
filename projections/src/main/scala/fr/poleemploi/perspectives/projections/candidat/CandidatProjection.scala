package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.{CandidatId, _}

import scala.concurrent.Future

trait CandidatProjection extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
    case e: CandidatConnecteEvent => onCandidatConnecteEvent(e)
    case e: ProfilCandidatModifieEvent => onProfilModifieEvent(e)
    case e: CriteresRechercheModifiesEvent => onCriteresRechercheModifiesEvent(e)
    case e: NumeroTelephoneModifieEvent => onNumeroTelephoneModifieEvent(e)
    case e: AdresseModifieeEvent => onAdresseModifieeEvent(e)
    case e: StatutDemandeurEmploiModifieEvent => onStatutDemandeurEmploiModifieEvent(e)
    case e: CVAjouteEvent => onCVAjouteEvent(e)
    case e: CVRemplaceEvent => onCVRemplaceEvent(e)
    case e: MRSAjouteeEvent => onMRSAjouteeEvent(e)
    case e: RepriseEmploiDeclareeParConseillerEvent => onRepriseEmploiDeclareeParConseillerEvent(e)
  }

  def candidatSaisieCriteresRecherche(query: CandidatSaisieCriteresRechercheQuery): Future[CandidatSaisieCriteresRechercheQueryResult]

  def candidatCriteresRecherche(query: CandidatCriteresRechercheQuery): Future[CandidatCriteresRechercheQueryResult]

  def candidatContactRecruteur(candidatId: CandidatId): Future[CandidatContactRecruteurDto]

  def rechercherCandidats(query: RechercherCandidatsQuery): Future[RechercheCandidatQueryResult]

  def listerPourConseiller(query: CandidatsPourConseillerQuery): Future[CandidatsPourConseillerQueryResult]

  def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit]

  def onCandidatConnecteEvent(event: CandidatConnecteEvent): Future[Unit]

  def onProfilModifieEvent(event: ProfilCandidatModifieEvent): Future[Unit]

  def onCriteresRechercheModifiesEvent(event: CriteresRechercheModifiesEvent): Future[Unit]

  def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit]

  def onStatutDemandeurEmploiModifieEvent(event: StatutDemandeurEmploiModifieEvent): Future[Unit]

  def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit]

  def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit]

  def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit]

  def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit]

  def onRepriseEmploiDeclareeParConseillerEvent(event: RepriseEmploiDeclareeParConseillerEvent): Future[Unit]
}
