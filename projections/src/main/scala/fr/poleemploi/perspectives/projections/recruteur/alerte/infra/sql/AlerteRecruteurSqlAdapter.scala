package fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.projections.recruteur.alerte._
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte, LocalisationAlerte}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{ResultSetConcurrency, ResultSetType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AlerteRecruteurSqlAdapter(database: Database) {

  import PostgresDriver.api._

  class AlerteRecruteurTable(tag: Tag) extends Table[AlerteRecruteurRecord](tag, "recruteurs_alertes") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def typeRecruteur = column[TypeRecruteur]("type_recruteur")

    def emailRecruteur = column[Email]("email_recruteur")

    def alerteId = column[AlerteId]("alerte_id")

    def frequence = column[FrequenceAlerte]("frequence")

    def codeROME = column[Option[CodeROME]]("metier")

    def codeSecteurActivite = column[Option[CodeSecteurActivite]]("secteur_activite")

    def labelLocalisation = column[Option[String]]("label_localisation")

    def latitude = column[Option[Double]]("latitude")

    def longitude = column[Option[Double]]("longitude")

    def * = (recruteurId, typeRecruteur, emailRecruteur, alerteId, frequence, codeROME, codeSecteurActivite, labelLocalisation, latitude, longitude) <> (AlerteRecruteurRecord.tupled, AlerteRecruteurRecord.unapply)
  }

  val alertesRecruteursTable = TableQuery[AlerteRecruteurTable]

  val alertesParIdQuery = Compiled { alerteId: Rep[AlerteId] =>
    alertesRecruteursTable
      .filter(_.alerteId === alerteId)
  }
  val alertesParFrequenceQuery = Compiled { frequenceAlerte: Rep[FrequenceAlerte] =>
    alertesRecruteursTable
      .filter(_.frequence === frequenceAlerte)
      .sortBy(_.id)
  }
  val modifierProfilQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      a <- alertesRecruteursTable if a.recruteurId === recruteurId
    } yield a.typeRecruteur
  }
  val modifierProfilGerantQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      a <- alertesRecruteursTable if a.recruteurId === recruteurId
    } yield a.emailRecruteur
  }
  val alertesParRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      a <- alertesRecruteursTable if a.recruteurId === recruteurId
    } yield a
  }

  def alertesParRecruteur(query: AlertesRecruteurQuery): Future[AlertesRecruteurQueryResult] =
    database.run(alertesParRecruteurQuery(query.recruteurId).result).map(r =>
      AlertesRecruteurQueryResult(
        alertes = r.toList.map(toAlerteRecruteurDto)
      )
    )

  def alertesQuotidiennes: Source[AlerteRecruteurDTO, NotUsed] =
    streamAlertes(FrequenceAlerte.QUOTIDIENNE)

  def alertesHebdomaraires: Source[AlerteRecruteurDTO, NotUsed] =
    streamAlertes(FrequenceAlerte.HEBDOMADAIRE)

  def onAlerteRecruteurCreeEvent(event: AlerteRecruteurCreeEvent): Future[Unit] =
    database
      .run(alertesRecruteursTable.map(a => (a.recruteurId, a.typeRecruteur, a.emailRecruteur, a.alerteId, a.frequence, a.codeROME, a.codeSecteurActivite, a.labelLocalisation, a.latitude, a.longitude))
        += (event.recruteurId, event.typeRecruteur, event.email, event.alerteId, event.frequence, event.codeROME, event.codeSecteurActivite, event.localisation.map(_.label), event.localisation.map(_.coordonnees.latitude), event.localisation.map(_.coordonnees.longitude)))
      .map(_ => ())

  def onAlerteRecruteurSupprimeeEvent(event: AlerteRecruteurSupprimeeEvent): Future[Unit] =
    database.run(alertesParIdQuery(event.alerteId).delete).map(_ => ())

  def onProfilModifieEvent(event: ProfilModifieEvent): Future[Unit] =
    database.run(modifierProfilQuery(event.recruteurId).update(
      event.typeRecruteur
    )).map(_ => ())

  def onProfilGerantModifieEvent(event: ProfilGerantModifieEvent): Future[Unit] =
    database.run(modifierProfilGerantQuery(event.recruteurId).update(
      event.email
    )).map(_ => ())

  private def streamAlertes(frequenceAlerte: FrequenceAlerte): Source[AlerteRecruteurDTO, NotUsed] =
    Source.fromPublisher {
      database.stream(
        alertesParFrequenceQuery(frequenceAlerte)
          .result
          .transactionally
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
      ).mapResult(toAlerteRecruteurDto)
    }

  // FIXME : mapping direct
  private def toAlerteRecruteurDto(alerteRecruteurRecord: AlerteRecruteurRecord): AlerteRecruteurDTO =
    AlerteRecruteurDTO(
      recruteurId = alerteRecruteurRecord.recruteurId,
      typeRecruteur = alerteRecruteurRecord.typeRecruteur,
      email = alerteRecruteurRecord.emailRecruteur,
      alerteId = alerteRecruteurRecord.alerteId,
      frequence = alerteRecruteurRecord.frequence,
      codeSecteurActivite = alerteRecruteurRecord.codeSecteurActivite,
      codeROME = alerteRecruteurRecord.codeROME,
      localisation = buildLocalisation(alerteRecruteurRecord)
    )

  private def buildLocalisation(alerteRecruteurRecord: AlerteRecruteurRecord): Option[LocalisationAlerte] =
    for {
      label <- alerteRecruteurRecord.labelLocalisation
      latitude <- alerteRecruteurRecord.latitude
      longitude <- alerteRecruteurRecord.longitude
    } yield
      LocalisationAlerte(
        label = label,
        coordonnees = Coordonnees(
          latitude = latitude,
          longitude = longitude
        )
      )
}
