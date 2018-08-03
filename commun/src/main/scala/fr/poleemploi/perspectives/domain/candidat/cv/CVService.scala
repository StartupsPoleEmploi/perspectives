package fr.poleemploi.perspectives.domain.candidat.cv

import java.nio.file.Path

import fr.poleemploi.perspectives.domain.candidat.CandidatId

import scala.concurrent.Future

trait CVService {

  def nextIdentity: CVId

  def save(cvId: CVId,
           candidatId: CandidatId,
           nomFichier: String,
           typeMedia: String,
           path: Path): Future[Unit]

  def update(cvId: CVId,
             nomFichier: String,
             typeMedia: String,
             path: Path): Future[Unit]

  def findDetailsCvByCandidat(candidatId: CandidatId): Future[Option[DetailsCV]]

  def getCvByCandidat(candidatId: CandidatId): Future[CV]
}
