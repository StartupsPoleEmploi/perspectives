package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.Future

trait CandidatProjectionQuery {

  def saisieCriteresRecherche(query: CandidatSaisieCriteresRechercheQuery): Future[CandidatSaisieCriteresRechercheQueryResult]

  def saisieDisponibilites(query: CandidatSaisieDisponibilitesQuery): Future[CandidatSaisieDisponibilitesQueryResult]

  def localisation(query: CandidatLocalisationQuery): Future[CandidatLocalisationQueryResult]

  def metiersValides(query: CandidatMetiersValidesQuery): Future[CandidatMetiersValidesQueryResult]

  def rechercheOffre(query: CandidatPourRechercheOffreQuery): Future[CandidatPourRechercheOffreQueryResult]

  def candidatContactRecruteur(candidatId: CandidatId): Future[CandidatContactRecruteurQueryResult]

  def secteursActivitesAvecCandidats(query: SecteursActivitesAvecCandidatsQuery): Future[SecteursActivitesAvecCandidatsQueryResult]

  def rechercherCandidats(query: RechercheCandidatsQuery): Future[RechercheCandidatQueryResult]

  def listerPourConseiller(query: CandidatsPourConseillerQuery): Future[CandidatsPourConseillerQueryResult]

  def existeCandidat(query: ExisteCandidatQuery): Future[ExisteCandidatQueryResult]

  def listerPourBatchDisponibilites(query: CandidatsPourBatchDisponibilitesQuery): Future[CandidatsPourBatchDisponibilitesQueryResult]

  def listerPourBatchJVR(query: CandidatsPourBatchJVRQuery): Future[CandidatsPourBatchJVRQueryResult]

  def listerPourCsv(query: CandidatsPourCsvQuery.type): Future[CandidatsPourCsvQueryResult]
}
