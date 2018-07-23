package fr.poleemploi.perspectives.projections.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.{AggregateId, Event}
import fr.poleemploi.perspectives.domain.recruteur._
import fr.poleemploi.perspectives.domain.{Genre, NumeroTelephone}
import fr.poleemploi.perspectives.projections.infra.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class RecruteurDto(recruteurId: String,
                        nom: String,
                        prenom: String,
                        email: String,
                        genre: Genre,
                        typeRecruteur: Option[TypeRecruteur],
                        raisonSociale: Option[String],
                        numeroSiret: Option[NumeroSiret],
                        numeroTelephone: Option[NumeroTelephone],
                        contactParCandidats: Option[Boolean],
                        dateInscription: ZonedDateTime)

class RecruteurProjection(val driver: PostgresDriver,
                          database: Database) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurEvent])

  override def isReplayable: Boolean = true

  override def onEvent(aggregateId: AggregateId): ReceiveEvent = {
    case e: RecruteurInscrisEvent => onRecruteurInscrisEvent(aggregateId, e)
    case e: ProfilModifieEvent => onProfilModifieEvent(aggregateId, e)
    case e: ProfilRecruteurModifiePEConnectEvent => onProfilPEConnectModifieEvent(aggregateId, e)
  }

  import driver.api._

  class RecruteurTable(tag: Tag) extends Table[RecruteurRecord](tag, "recruteurs") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[String]("recruteur_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def genre = column[String]("genre")

    def email = column[String]("email")

    def typeRecruteur = column[Option[String]]("type_recruteur")

    def raisonSociale = column[Option[String]]("raison_sociale")

    def numeroSiret = column[Option[String]]("numero_siret")

    def numeroTelephone = column[Option[String]]("numero_telephone")

    def contactParCandidats = column[Option[Boolean]]("contact_par_candidats")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def * = (recruteurId, nom, prenom, genre, email, typeRecruteur, raisonSociale, numeroSiret, numeroTelephone, contactParCandidats, dateInscription) <> (RecruteurRecord.tupled, RecruteurRecord.unapply)
  }

  val recruteurTable = TableQuery[RecruteurTable]

  def getRecruteur(recruteurId: String): Future[RecruteurDto] = {
    val query = recruteurTable.filter(u => u.recruteurId === recruteurId)

    database.run(query.result.head).map(_.toRecruteurDto)
  }

  def findAllOrderByDateInscription: Future[List[RecruteurDto]] = {
    val query = recruteurTable.sortBy(_.dateInscription.desc)

    database.run(query.result).map(_.toList.map(_.toRecruteurDto))
  }

  private def onRecruteurInscrisEvent(aggregateId: AggregateId,
                                      event: RecruteurInscrisEvent): Future[Unit] =
    database
      .run(recruteurTable.map(
        r => (r.recruteurId, r.nom, r.prenom, r.genre, r.email, r.dateInscription))
        += (aggregateId.value, event.nom, event.prenom, event.genre, event.email, event.date))
      .map(_ => ())

  private def onProfilModifieEvent(aggregateId: AggregateId,
                                   event: ProfilModifieEvent): Future[Unit] = {
    val query = for {
      r <- recruteurTable if r.recruteurId === aggregateId.value
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

  private def onProfilPEConnectModifieEvent(aggregateId: AggregateId,
                                           event: ProfilRecruteurModifiePEConnectEvent): Future[Unit] = {
    val query = for {
      r <- recruteurTable if r.recruteurId === aggregateId.value
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

private[recruteur] case class RecruteurRecord(recruteurId: String,
                                              nom: String,
                                              prenom: String,
                                              genre: String,
                                              email: String,
                                              typeRecruteur: Option[String],
                                              raisonSociale: Option[String],
                                              numeroSiret: Option[String],
                                              numeroTelephone: Option[String],
                                              contactParCandidats: Option[Boolean],
                                              dateInscription: ZonedDateTime) {

  def toRecruteurDto: RecruteurDto = RecruteurDto(
    recruteurId = recruteurId,
    nom = nom,
    prenom = prenom,
    genre = Genre.from(genre).get,
    email = email,
    typeRecruteur = typeRecruteur.flatMap(TypeRecruteur.from),
    raisonSociale = raisonSociale,
    numeroSiret = numeroSiret.flatMap(NumeroSiret.from),
    numeroTelephone = numeroTelephone.flatMap(NumeroTelephone.from),
    contactParCandidats = contactParCandidats,
    dateInscription = dateInscription
  )
}
