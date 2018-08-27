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
  val getRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    recruteurTable.filter(r => r.recruteurId === recruteurId)
  }
  val listerParDateInscriptionQuery = recruteurTable.sortBy(_.dateInscription.desc)
  val modifierProfilQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield (r.typeRecruteur, r.raisonSociale, r.numeroSiret, r.numeroTelephone, r.contactParCandidats)
  }
  val modifierProfilPEConnectQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield (r.nom, r.prenom, r.email, r.genre)
  }

  def getRecruteur(query: GetRecruteurQuery): Future[RecruteurDto] =
    database.run(getRecruteurQuery(query.recruteurId).result.head)

  def listerParDateInscription: Future[List[RecruteurDto]] =
    database.run(listerParDateInscriptionQuery.result).map(_.toList)

  private def onRecruteurInscrisEvent(event: RecruteurInscrisEvent): Future[Unit] =
    database
      .run(recruteurTable.map(r => (r.recruteurId, r.nom, r.prenom, r.genre, r.email, r.dateInscription))
        += (event.recruteurId, event.nom, event.prenom, event.genre, event.email, event.date))
      .map(_ => ())

  private def onProfilModifieEvent(event: ProfilModifieEvent): Future[Unit] =
    database.run(modifierProfilQuery(event.recruteurId).update((
      Some(event.typeRecruteur),
      Some(event.raisonSociale),
      Some(event.numeroSiret),
      Some(event.numeroTelephone),
      Some(event.contactParCandidats)
    ))).map(_ => ())

  private def onProfilPEConnectModifieEvent(event: ProfilRecruteurModifiePEConnectEvent): Future[Unit] =
    database.run(modifierProfilPEConnectQuery(event.recruteurId).update((
      event.nom,
      event.prenom,
      event.email,
      event.genre
    ))).map(_ => ())
}
