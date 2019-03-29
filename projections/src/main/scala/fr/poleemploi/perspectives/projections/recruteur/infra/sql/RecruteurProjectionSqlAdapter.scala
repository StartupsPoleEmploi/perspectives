package fr.poleemploi.perspectives.projections.recruteur.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.recruteur._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurProjectionSqlAdapter(database: Database) {

  import PostgresDriver.api._

  class RecruteurTable(tag: Tag) extends Table[RecruteurRecord](tag, "recruteurs") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def nom = column[Nom]("nom")

    def prenom = column[Prenom]("prenom")

    def email = column[Email]("email")

    def genre = column[Genre]("genre")

    def typeRecruteur = column[Option[TypeRecruteur]]("type_recruteur")

    def raisonSociale = column[Option[String]]("raison_sociale")

    def numeroSiret = column[Option[NumeroSiret]]("numero_siret")

    def numeroTelephone = column[Option[NumeroTelephone]]("numero_telephone")

    def contactParCandidats = column[Option[Boolean]]("contact_par_candidats")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def dateDerniereConnexion = column[ZonedDateTime]("date_derniere_connexion")

    def * = (recruteurId, nom, prenom, email, genre, typeRecruteur, raisonSociale, numeroSiret, numeroTelephone, contactParCandidats, dateInscription, dateDerniereConnexion) <> (RecruteurRecord.tupled, RecruteurRecord.unapply)
  }

  implicit object ProfilRecruteurShape extends CaseClassShape(ProfilRecruteurLifted.tupled, ProfilRecruteurQueryResult.tupled)

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
  val listerParDateInscriptionQuery = Compiled { (nbRecruteursParPage: ConstColumn[Long], avantDateInscription: Rep[ZonedDateTime]) =>
    recruteurTable
      .filter(_.dateInscription < avantDateInscription)
      .sortBy(_.dateInscription.desc)
      .take(nbRecruteursParPage)
      .map(r => RecruteurPourConseillerLifted(r.recruteurId, r.nom, r.prenom, r.email, r.genre, r.typeRecruteur, r.raisonSociale, r.numeroSiret, r.numeroTelephone, r.contactParCandidats, r.dateInscription, r.dateDerniereConnexion))
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
  val derniereConnexionQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield r.dateDerniereConnexion
  }

  def typeRecruteur(query: TypeRecruteurQuery): Future[TypeRecruteurQueryResult] =
    database.run(typeRecruteurQuery(query.recruteurId).result.head).map(TypeRecruteurQueryResult)

  def profilRecruteur(query: ProfilRecruteurQuery): Future[ProfilRecruteurQueryResult] =
    database.run(profilRecruteurQuery(query.recruteurId).result.head)

  def listerPourConseiller(query: RecruteursPourConseillerQuery): Future[RecruteursPourConseillerQueryResult] =
    database.run(listerParDateInscriptionQuery(query.nbRecruteursParPage * query.nbPagesACharger, query.page.map(_.dateInscription).getOrElse(ZonedDateTime.now())).result)
      .map(r => {
        val recruteurs = r.toList
        val pages = recruteurs.zipWithIndex
          .filter(v => v._2 == 0 || (v._2 + 1) % query.nbRecruteursParPage == 0)
          .map(v => KeysetRecruteursPourConseiller(
            dateInscription = if (v._2 == 0) v._1.dateInscription.plusSeconds(1) else v._1.dateInscription,
            recruteurId = v._1.recruteurId
          ))

        RecruteursPourConseillerQueryResult(
          recruteurs = recruteurs.take(query.nbRecruteursParPage),
          pages = query.page.map(k => k :: pages.tail).getOrElse(pages),
          pageSuivante = pages.reverse.headOption
        )
      })

  def onRecruteurInscritEvent(event: RecruteurInscritEvent): Future[Unit] =
    database
      .run(recruteurTable.map(r => (r.recruteurId, r.nom, r.prenom, r.genre, r.email, r.dateInscription, r.dateDerniereConnexion))
        += (event.recruteurId, event.nom, event.prenom, event.genre, event.email, event.date, event.date))
      .map(_ => ())

  def onRecruteurConnecteEvent(event: RecruteurConnecteEvent): Future[Unit] =
    database.run(derniereConnexionQuery(event.recruteurId).update(
      event.date
    )).map(_ => ())

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
