package fr.poleemploi.perspectives.commun.infra.peconnect

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Récupère les informations issues des APIs PEConnect pour un candidat. <br />
  * Les appels aux APIs se font les uns après les autres, inutile de délencher autant de commandes différentes sur l'aggrégat que d'appels d'API (qui vont forcément échouées et vont devoir être réessayées au niveau des événements)
  */
class PEConnectCandidatProcessManager(peConnectAccessTokenStorage: PEConnectAccessTokenStorage,
                                      peConnectWSAdapter: PEConnectWSAdapter,
                                      candidatCommandHandler: CandidatCommandHandler) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatInscritEvent], classOf[CandidatConnecteEvent])

  override def isReplayable: Boolean = false

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => modifierProfilCandidat(e.candidatId)
    case e: CandidatConnecteEvent => modifierProfilCandidat(e.candidatId)
  }

  /**
    * Pour dinstinguer le cas d'échec du service et le cas d'une information supprimée, on renvoit None au lieu de Nil en cas d'échec
    */
  implicit class PEConnectCandidatServiceRecovery[T](result: Future[T]) {

    def getOrRecoverWithNone(candidatId: CandidatId): Future[Option[T]] =
      result.map(Some(_)) recover {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des infos du candidat ${candidatId.value}", t)
          None
      }
  }

  def modifierProfilCandidat(candidatId: CandidatId): Future[Unit] =
    for {
      accessToken <- peConnectAccessTokenStorage.find(candidatId).map(_.getOrElse(throw new IllegalArgumentException(s"Pas de token pour le candidat ${candidatId.value}")))
      adresse <- peConnectWSAdapter.coordonneesCandidat(accessToken).getOrRecoverWithNone(candidatId)
      statutDemandeurEmploi <- peConnectWSAdapter.statutDemandeurEmploiCandidat(accessToken).getOrRecoverWithNone(candidatId)
      centresInteret <- peConnectWSAdapter.centresInteretCandidat(accessToken).getOrRecoverWithNone(candidatId)
      langues <- peConnectWSAdapter.languesCandidat(accessToken).getOrRecoverWithNone(candidatId)
      permis <- peConnectWSAdapter.permisCandidat(accessToken).getOrRecoverWithNone(candidatId)
      competences <- peConnectWSAdapter.competencesCandidat(accessToken).getOrRecoverWithNone(candidatId)
      formations <- peConnectWSAdapter.formationsCandidat(accessToken).getOrRecoverWithNone(candidatId)
      experiencesProfessionnelles <- peConnectWSAdapter.experiencesProfessionnellesCandidat(accessToken).getOrRecoverWithNone(candidatId)
      _ <- candidatCommandHandler.handle(
        ModifierProfilCandidatCommand(
          id = candidatId,
          adresse = adresse,
          statutDemandeurEmploi = statutDemandeurEmploi,
          centresInteret = centresInteret,
          langues = langues,
          permis = permis,
          savoirEtre = competences.map(_._1),
          savoirFaire = competences.map(_._2),
          formations = formations,
          experiencesProfessionnelles = experiencesProfessionnelles
        )
      )
    } yield ()
}

