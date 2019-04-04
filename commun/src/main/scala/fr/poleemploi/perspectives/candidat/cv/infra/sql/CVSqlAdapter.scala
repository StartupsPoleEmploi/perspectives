package fr.poleemploi.perspectives.candidat.cv.infra.sql

import java.nio.file.{Files, Path}
import java.time.ZonedDateTime
import java.util.UUID

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain._
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private[infra] case class CVRecord(candidatId: CandidatId,
                                   cvId: CVId,
                                   nomFichier: String,
                                   typeMedia: TypeMedia,
                                   fichier: Array[Byte],
                                   date: ZonedDateTime)

class CVSqlAdapter(val driver: PostgresDriver,
                   database: Database) extends CVService {

  import driver.api._

  class CVCandidatTable(tag: Tag) extends Table[CVRecord](tag, "candidats_cv") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def cvId = column[CVId]("cv_id")

    def nomFichier = column[String]("nom_fichier")

    def typeMedia = column[TypeMedia]("type_media")

    def fichier = column[Array[Byte]]("fichier")

    def date = column[ZonedDateTime]("date")

    def * = (candidatId, cvId, nomFichier, typeMedia, fichier, date) <> (CVRecord.tupled, CVRecord.unapply)
  }

  val cvCandidatTable = TableQuery[CVCandidatTable]
  val findDetailsCVByCandidatQuery = Compiled { candidatId: Rep[CandidatId] =>
    cvCandidatTable
      .filter(c => c.candidatId === candidatId)
      .map(c => (c.cvId, c.nomFichier, c.typeMedia, c.date))
  }
  val getCVByCandidatQuery = Compiled { candidatId: Rep[CandidatId] =>
    cvCandidatTable.filter(c => c.candidatId === candidatId)
  }
  val updateCVQuery = Compiled { cvId: Rep[CVId] =>
    for {
      c <- cvCandidatTable if c.cvId === cvId
    } yield (c.nomFichier, c.fichier, c.typeMedia, c.date)
  }

  override def nextIdentity: CVId = CVId(UUID.randomUUID().toString)

  override def save(cvId: CVId,
                    candidatId: CandidatId,
                    nomFichier: String,
                    typeMedia: TypeMedia,
                    path: Path): Future[Unit] =
    database
      .run(cvCandidatTable.map(
        c => (c.cvId, c.candidatId, c.nomFichier, c.fichier, c.typeMedia, c.date))
        += (cvId, candidatId, s"$nomFichier.${TypeMedia.getExtensionFichier(typeMedia)}", Files.readAllBytes(path), typeMedia, ZonedDateTime.now()))
      .map(_ => ())

  override def update(cvId: CVId,
                      nomFichier: String,
                      typeMedia: TypeMedia,
                      path: Path): Future[Unit] =
    database.run(updateCVQuery(cvId).update((
      s"$nomFichier.${TypeMedia.getExtensionFichier(typeMedia)}",
      Files.readAllBytes(path),
      typeMedia,
      ZonedDateTime.now()
    ))).map(_ => ())

  override def findDetailsCVByCandidat(candidatId: CandidatId): Future[Option[DetailsCV]] =
    database.run(findDetailsCVByCandidatQuery(candidatId).result.headOption).map(_.map(c => DetailsCV(
      id = c._1,
      nomFichier = c._2,
      typeMedia = c._3,
      date = c._4
    )))

  override def getCVByCandidat(candidatId: CandidatId): Future[CV] =
    database.run(getCVByCandidatQuery(candidatId).result.head).map(c => CV(
      id = c.cvId,
      data = c.fichier,
      typeMedia = c.typeMedia,
      nomFichier = c.nomFichier,
      date = c.date
    ))
}
