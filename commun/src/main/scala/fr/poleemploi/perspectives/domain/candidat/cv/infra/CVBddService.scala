package fr.poleemploi.perspectives.domain.candidat.cv.infra

import java.nio.file.{Files, Path}
import java.time.ZonedDateTime

import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.candidat.cv._
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CVCandidatRecord(candidatId: String,
                            cvId: String,
                            nomFichier: String,
                            fichier: Array[Byte],
                            typeMedia: String,
                            hash: String,
                            date: ZonedDateTime)

class CVBddService(val driver: PostgresDriver,
                   database: Database) extends CVService {

  import driver.api._

  class CVCandidatTable(tag: Tag) extends Table[CVCandidatRecord](tag, "candidats_cv") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[String]("candidat_id")

    def cvId = column[String]("cv_id")

    def nomFichier = column[String]("nom_fichier")

    def fichier = column[Array[Byte]]("fichier")

    def typeMedia = column[String]("type_media")

    def hash = column[String]("hash")

    def date = column[ZonedDateTime]("date")

    def * = (candidatId, cvId, nomFichier, fichier, typeMedia, hash, date) <> (CVCandidatRecord.tupled, CVCandidatRecord.unapply)
  }

  val largeObjectImport = SimpleFunction.unary[String, Long]("lo_import")

  val cvCandidatTable = TableQuery[CVCandidatTable]

  override def findByCandidat(candidatId: CandidatId): Future[Option[CVCandidat]] = {
    val query = cvCandidatTable.filter(c => c.candidatId === candidatId.value)
        .map(c => (c.cvId, c.candidatId))

    database.run(query.result.headOption).map(_.map(r => CVCandidat(
      cvId = CVId(r._1),
      candidatId = CandidatId(r._2)
    )))
  }

  override def save(cvId: CVId,
                    candidatId: CandidatId,
                    nomFichier: String,
                    typeMedia: String,
                    path: Path): Future[Unit] =
    database
      .run(cvCandidatTable.map(
        c => (c.cvId, c.candidatId, c.nomFichier, c.fichier, c.typeMedia, c.hash, c.date))
        += (cvId.value, candidatId.value, nomFichier, Files.readAllBytes(path), typeMedia, "", ZonedDateTime.now()))
      .map(_ => ())

  override def update(cvId: CVId,
                      candidatId: CandidatId,
                      nomFichier: String,
                      typeMedia: String,
                      path: Path): Future[Unit] = {
    val query = for {
      c <- cvCandidatTable if c.candidatId === candidatId.value
    } yield (c.nomFichier, c.fichier, c.typeMedia, c.hash, c.date)
    val updateAction = query.update((
      nomFichier,
      Files.readAllBytes(path),
      typeMedia,
      "",
      ZonedDateTime.now()
    ))

    database.run(updateAction).map(_ => ())
  }

  override def findCvByCandidat(candidatId: CandidatId): Future[Option[CV]] = {
    val query = cvCandidatTable
      .filter(c => c.candidatId === candidatId.value)
      .map(c => (c.cvId, c.nomFichier, c.typeMedia, c.date))

    database.run(query.result.headOption).map(_.map(c => CV(
      id = CVId(c._1),
      nomFichier = c._2,
      typeMedia = c._3,
      date = c._4
    )))
  }

  override def getByCandidat(candidatId: CandidatId): Future[FichierCV] = {
    val query = cvCandidatTable
      .filter(c => c.candidatId === candidatId.value)

    database.run(query.result.head).map(c => FichierCV(
      id = CVId(c.cvId),
      data = c.fichier,
      typeMedia = c.typeMedia,
      nomFichier = c.nomFichier
    ))
  }

  override def getFichierCV(cvId: CVId): Future[FichierCV] = {
    val query = cvCandidatTable
      .filter(c => c.cvId === cvId.value)

    database.run(query.result.head).map(c => FichierCV(
      id = CVId(c.cvId),
      data = c.fichier,
      typeMedia = c.typeMedia,
      nomFichier = c.nomFichier
    ))
  }
}
