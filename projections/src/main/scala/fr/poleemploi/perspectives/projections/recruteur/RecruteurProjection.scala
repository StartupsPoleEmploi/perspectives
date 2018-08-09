package fr.poleemploi.perspectives.projections.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.recruteur._
import fr.poleemploi.perspectives.domain.{Genre, NumeroTelephone}
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurProjection(val driver: PostgresDriver,
                          database: Database) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: RecruteurInscrisEvent => onRecruteurInscrisEvent(e)
    case e: ProfilModifieEvent => onProfilModifieEvent(e)
    case e: ProfilRecruteurModifiePEConnectEvent => onProfilPEConnectModifieEvent(e)
  }

  import driver.api._

  class RecruteurTable(tag: Tag) extends Table[RecruteurDto](tag, "recruteurs") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def email = column[String]("email")

    def genre = column[Genre]("genre")

    def typeRecruteur = column[Option[TypeRecruteur]]("type_recruteur")

    def raisonSociale = column[Option[String]]("raison_sociale")

    def numeroSiret = column[Option[NumeroSiret]]("numero_siret")

    def numeroTelephone = column[Option[NumeroTelephone]]("numero_telephone")

    def contactParCandidats = column[Option[Boolean]]("contact_par_candidats")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def * = (recruteurId, nom, prenom, email, genre, typeRecruteur, raisonSociale, numeroSiret, numeroTelephone, contactParCandidats, dateInscription) <> (RecruteurDto.tupled, RecruteurDto.unapply)
  }

  val recruteurTable = TableQuery[RecruteurTable]

  def getRecruteur(query: GetRecruteurQuery): Future[RecruteurDto] = {
    val select = recruteurTable.filter(r => r.recruteurId === query.recruteurId)

    database.run(select.result.head)
  }

  def findAllOrderByDateInscription: Future[List[RecruteurDto]] = {
    val select = recruteurTable.sortBy(_.dateInscription.desc)

    database.run(select.result).map(_.toList)
  }

  private def onRecruteurInscrisEvent(event: RecruteurInscrisEvent): Future[Unit] =
    database
      .run(recruteurTable.map(r => (r.recruteurId, r.nom, r.prenom, r.genre, r.email, r.dateInscription))
        += (event.recruteurId, event.nom, event.prenom, event.genre, event.email, event.date))
      .map(_ => ())

  private def onProfilModifieEvent(event: ProfilModifieEvent): Future[Unit] = {
    val query = for {
      r <- recruteurTable if r.recruteurId === event.recruteurId
    } yield (r.typeRecruteur, r.raisonSociale, r.numeroSiret, r.numeroTelephone, r.contactParCandidats)
    val updateAction = query.update((
      Some(event.typeRecruteur),
      Some(event.raisonSociale),
      Some(event.numeroSiret),
      Some(event.numeroTelephone),
      Some(event.contactParCandidats)
    ))

    database.run(updateAction).map(_ => ())
  }

  private def onProfilPEConnectModifieEvent(event: ProfilRecruteurModifiePEConnectEvent): Future[Unit] = {
    val query = for {
      r <- recruteurTable if r.recruteurId === event.recruteurId
    } yield (r.nom, r.prenom, r.email, r.genre)
    val updateAction = query.update((
      event.nom,
      event.prenom,
      event.email,
      event.genre
    ))

    database.run(updateAction).map(_ => ())
  }
}
