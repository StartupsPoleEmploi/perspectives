package fr.poleemploi.perspectives.projections.recruteur.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.projections.recruteur.{ProfilRecruteurDto, ProfilRecruteurQuery, RecruteurPourConseillerDto}
import fr.poleemploi.perspectives.recruteur._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurProjectionSqlAdapter(database: Database) {

  import PostgresDriver.api._

  class RecruteurTable(tag: Tag) extends Table[RecruteurRecord](tag, "recruteurs") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def email = column[Email]("email")

    def genre = column[Genre]("genre")

    def typeRecruteur = column[Option[TypeRecruteur]]("type_recruteur")

    def raisonSociale = column[Option[String]]("raison_sociale")

    def numeroSiret = column[Option[NumeroSiret]]("numero_siret")

    def numeroTelephone = column[Option[NumeroTelephone]]("numero_telephone")

    def contactParCandidats = column[Option[Boolean]]("contact_par_candidats")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def * = (recruteurId, nom, prenom, email, genre, typeRecruteur, raisonSociale, numeroSiret, numeroTelephone, contactParCandidats, dateInscription) <> (RecruteurRecord.tupled, RecruteurRecord.unapply)
  }

  implicit object ProfilRecruteurShape extends CaseClassShape(ProfilRecruteurLifted.tupled, ProfilRecruteurDto.tupled)

  implicit object RecruteurPourConseillerShape extends CaseClassShape(RecruteurPourConseillerLifted.tupled, RecruteurPourConseillerDto.tupled)

  val recruteurTable = TableQuery[RecruteurTable]
  val typeRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    recruteurTable
      .filter(r => r.recruteurId === recruteurId)
      .map(_.typeRecruteur)
  }
  val profilRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    recruteurTable
      .filter(r => r.recruteurId === recruteurId)
      .map(r => ProfilRecruteurLifted(r.recruteurId, r.typeRecruteur, r.raisonSociale, r.numeroSiret, r.numeroTelephone, r.contactParCandidats))
  }
  val listerParDateInscriptionQuery = Compiled {
    recruteurTable
      .sortBy(_.dateInscription.desc)
      .map(r => RecruteurPourConseillerLifted(r.recruteurId, r.nom, r.prenom, r.email, r.genre, r.typeRecruteur, r.raisonSociale, r.numeroSiret, r.numeroTelephone, r.contactParCandidats, r.dateInscription))
  }
  val modifierProfilQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield (r.typeRecruteur, r.raisonSociale, r.numeroSiret, r.numeroTelephone, r.contactParCandidats)
  }
  val modifierProfilGerantQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield (r.nom, r.prenom, r.email, r.genre)
  }

  def typeRecruteur(recruteurId: RecruteurId): Future[Option[TypeRecruteur]] =
    database.run(typeRecruteurQuery(recruteurId).result.head)

  def profilRecruteur(query: ProfilRecruteurQuery): Future[ProfilRecruteurDto] =
    database.run(profilRecruteurQuery(query.recruteurId).result.head)

  def listerParDateInscriptionPourConseiller: Future[List[RecruteurPourConseillerDto]] =
    database.run(listerParDateInscriptionQuery.result).map(_.toList)

  def onRecruteurInscritEvent(event: RecruteurInscritEvent): Future[Unit] =
    database
      .run(recruteurTable.map(r => (r.recruteurId, r.nom, r.prenom, r.genre, r.email, r.dateInscription))
        += (event.recruteurId, event.nom, event.prenom, event.genre, event.email, event.date))
      .map(_ => ())

  def onProfilModifieEvent(event: ProfilModifieEvent): Future[Unit] =
    database.run(modifierProfilQuery(event.recruteurId).update((
      Some(event.typeRecruteur),
      Some(event.raisonSociale),
      Some(event.numeroSiret),
      Some(event.numeroTelephone),
      Some(event.contactParCandidats)
    ))).map(_ => ())

  def onProfilGerantModifieEvent(event: ProfilGerantModifieEvent): Future[Unit] =
    database.run(modifierProfilGerantQuery(event.recruteurId).update((
      event.nom,
      event.prenom,
      event.email,
      event.genre
    ))).map(_ => ())

}
