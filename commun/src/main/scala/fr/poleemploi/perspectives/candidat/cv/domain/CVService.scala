package fr.poleemploi.perspectives.candidat.cv.domain

import java.nio.file.Path

import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.Future

trait CVService {

  def nextIdentity: CVId

  def save(cvId: CVId,
           candidatId: CandidatId,
           nomFichier: String,
           typeMedia: TypeMedia,
           path: Path): Future[Unit]

  def update(cvId: CVId,
             nomFichier: String,
             typeMedia: TypeMedia,
             path: Path): Future[Unit]

  def findDetailsCVByCandidat(candidatId: CandidatId): Future[Option[DetailsCV]]

  def getCVByCandidat(candidatId: CandidatId): Future[CV]
}
