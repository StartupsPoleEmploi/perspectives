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

  def modifierProfilCandidat(candidatId: CandidatId): Future[Unit] =
    for {
      accessToken <- peConnectAccessTokenStorage.find(candidatId).map(_.getOrElse(throw new IllegalArgumentException(s"Pas de token pour le candidat ${candidatId.value}")))
      adresse <- peConnectWSAdapter.coordonneesCandidat(accessToken).map(Some(_)).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des coordonnées du candidat ${candidatId.value}", t)
          Future.successful(None)
      }
      statutDemandeurEmploi <- peConnectWSAdapter.statutDemandeurEmploiCandidat(accessToken).map(Some(_)).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération du statut de demandeur d'emploi du candidat ${candidatId.value}", t)
          Future.successful(None)
      }
      centresInteret <- peConnectWSAdapter.centresInteretCandidat(accessToken).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des centres d'intérêt du candidat ${candidatId.value}", t)
          Future.successful(Nil)
      }
      langues <- peConnectWSAdapter.languesCandidat(accessToken).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des langues du candidat ${candidatId.value}", t)
          Future.successful(Nil)
      }
      permis <- peConnectWSAdapter.permisCandidat(accessToken).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des permis du candidat ${candidatId.value}", t)
          Future.successful(Nil)
      }
      (savoirEtre, savoirFaire) <- peConnectWSAdapter.competencesCandidat(accessToken).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des compétences du candidat ${candidatId.value}", t)
          Future.successful((Nil, Nil))
      }
      formations <- peConnectWSAdapter.formationsCandidat(accessToken).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des formations du candidat ${candidatId.value}", t)
          Future.successful(Nil)
      }
      experiencesProfessionnelles <- peConnectWSAdapter.experiencesProfessionnelles(accessToken).recoverWith {
        case t: Throwable =>
          peConnectLogger.error(s"Erreur lors de la récupération des expériences professionnelles du candidat ${candidatId.value}", t)
          Future.successful(Nil)
      }
      _ <- candidatCommandHandler.handle(
        ModifierProfilCandidatCommand(
          id = candidatId,
          adresse = adresse,
          statutDemandeurEmploi = statutDemandeurEmploi,
          centresInteret = centresInteret,
          langues = langues,
          permis = permis,
          savoirEtre = savoirEtre,
          savoirFaire = savoirFaire,
          formations = formations,
          experiencesProfessionnelles = experiencesProfessionnelles
        )
      )
    } yield ()
}

