package fr.poleemploi.perspectives.domain.candidat.cv.infra

import java.nio.file.{Files, Path}
import java.time.ZonedDateTime
import java.util.UUID

import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.candidat.cv._
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private[infra] case class CVRecord(candidatId: CandidatId,
                                   cvId: CVId,
                                   nomFichier: String,
                                   typeMedia: String,
                                   fichier: Array[Byte],
                                   date: ZonedDateTime)

class CVBddService(val driver: PostgresDriver,
                   database: Database) extends CVService {

  import driver.api._

  class CVCandidatTable(tag: Tag) extends Table[CVRecord](tag, "candidats_cv") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def cvId = column[CVId]("cv_id")

    def nomFichier = column[String]("nom_fichier")

    def typeMedia = column[String]("type_media")

    def fichier = column[Array[Byte]]("fichier")

    def date = column[ZonedDateTime]("date")

    def * = (candidatId, cvId, nomFichier, typeMedia, fichier, date) <> (CVRecord.tupled, CVRecord.unapply)
  }

  val cvCandidatTable = TableQuery[CVCandidatTable]

  override def nextIdentity: CVId = CVId(UUID.randomUUID().toString)

  override def save(cvId: CVId,
                    candidatId: CandidatId,
                    nomFichier: String,
                    typeMedia: String,
                    path: Path): Future[Unit] =
    database
      .run(cvCandidatTable.map(
        c => (c.cvId, c.candidatId, c.nomFichier, c.fichier, c.typeMedia, c.date))
        += (cvId, candidatId, nomFichier, Files.readAllBytes(path), typeMedia, ZonedDateTime.now()))
      .map(_ => ())

  override def update(cvId: CVId,
                      nomFichier: String,
                      typeMedia: String,
                      path: Path): Future[Unit] = {
    val query = for {
      c <- cvCandidatTable if c.cvId === cvId
    } yield (c.nomFichier, c.fichier, c.typeMedia, c.date)
    val updateAction = query.update((
      nomFichier,
      Files.readAllBytes(path),
      typeMedia,
      ZonedDateTime.now()
    ))

    database.run(updateAction).map(_ => ())
  }

  override def findDetailsCvByCandidat(candidatId: CandidatId): Future[Option[DetailsCV]] = {
    val query = cvCandidatTable
      .filter(c => c.candidatId === candidatId)
      .map(c => (c.cvId, c.nomFichier, c.typeMedia, c.date))

    database.run(query.result.headOption).map(_.map(c => DetailsCV(
      id = c._1,
      nomFichier = c._2,
      typeMedia = c._3,
      date = c._4
    )))
  }

  override def getCvByCandidat(candidatId: CandidatId): Future[CV] = {
    val query = cvCandidatTable
      .filter(c => c.candidatId === candidatId)

    database.run(query.result.head).map(c => CV(
      id = c.cvId,
      data = c.fichier,
      typeMedia = c.typeMedia,
      nomFichier = c.nomFichier,
      date = c.date
    ))
  }
}
