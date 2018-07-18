package fr.poleemploi.perspectives.projections.candidat.cv

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.candidat.cv.{CVCandidat, CVId, CVService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class DetailsCVDto(id: CVId,
                        nomFichier: String,
                        typeMedia: String,
                        date: ZonedDateTime)

case class FichierCvDto(id: CVId,
                        nomFichier: String,
                        data: Array[Byte],
                        typeMedia: String)

class CvProjection(cvService: CVService) {

  def findByCandidat(candidatId: CandidatId): Future[Option[CVCandidat]] =
    cvService.findByCandidat(candidatId)

  def findCvByCandidat(candidatId: CandidatId): Future[Option[DetailsCVDto]] =
    cvService.findCvByCandidat(candidatId).map(_.map(cv =>
      DetailsCVDto(
        id = cv.id,
        nomFichier = cv.nomFichier,
        typeMedia = cv.typeMedia,
        date = cv.date
      )
    ))

  def getByCandidat(candidatId: CandidatId): Future[FichierCvDto] =
    cvService.getByCandidat(candidatId).map(fichierCv => FichierCvDto(
      id = fichierCv.id,
      data = fichierCv.data,
      typeMedia = fichierCv.typeMedia,
      nomFichier = fichierCv.nomFichier
    ))

  def getFichierCv(cvId: CVId): Future[FichierCvDto] =
    cvService.getFichierCV(cvId = cvId).map(fichierCv => FichierCvDto(
      id = fichierCv.id,
      data = fichierCv.data,
      typeMedia = fichierCv.typeMedia,
      nomFichier = fichierCv.nomFichier
    ))
}
