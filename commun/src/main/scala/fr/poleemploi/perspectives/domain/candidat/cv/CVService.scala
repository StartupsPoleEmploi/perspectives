package fr.poleemploi.perspectives.domain.candidat.cv

import java.nio.file.Path

import fr.poleemploi.perspectives.domain.candidat.CandidatId

import scala.concurrent.Future

trait CVService {

  def save(cvId: CVId,
           candidatId: CandidatId,
           nomFichier: String,
           typeMedia: String,
           path: Path): Future[Unit]

  def update(cvId: CVId,
             candidatId: CandidatId,
             nomFichier: String,
             typeMedia: String,
             path: Path): Future[Unit]

  def findByCandidat(candidatId: CandidatId): Future[Option[CVCandidat]]

  def findCvByCandidat(candidatId: CandidatId): Future[Option[CV]]

  def getFichierCV(cvId: CVId): Future[FichierCV]

  def getByCandidat(candidatId: CandidatId): Future[FichierCV]
}
