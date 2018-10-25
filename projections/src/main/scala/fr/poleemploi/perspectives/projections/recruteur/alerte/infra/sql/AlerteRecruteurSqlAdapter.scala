package fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.projections.recruteur.AlertesRecruteurQuery
import fr.poleemploi.perspectives.projections.recruteur.alerte._
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{ResultSetConcurrency, ResultSetType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AlerteRecruteurSqlAdapter(database: Database,
                                rechercheCandidatService: RechercheCandidatService) {

  import PostgresDriver.api._

  class AlerteRecruteurTable(tag: Tag) extends Table[AlerteRecruteurRecord](tag, "alertes_recruteurs") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def prenomRecruteur = column[String]("prenom_recruteur")

    def typeRecruteur = column[TypeRecruteur]("type_recruteur")

    def emailRecruteur = column[Email]("email_recruteur")

    def alerteId = column[AlerteId]("alerte_id")

    def frequence = column[FrequenceAlerte]("frequence")

    def codeROME = column[Option[CodeROME]]("metier")

    def codeSecteurActivite = column[Option[CodeSecteurActivite]]("secteur_activite")

    def codeDepartement = column[Option[CodeDepartement]]("departement")

    def * = (recruteurId, prenomRecruteur, typeRecruteur, emailRecruteur, alerteId, frequence, codeROME, codeSecteurActivite, codeDepartement) <> (AlerteRecruteurRecord.tupled, AlerteRecruteurRecord.unapply)
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
    } yield (a.prenomRecruteur, a.emailRecruteur)
  }
  val alertesParRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      a <- alertesRecruteursTable if a.recruteurId === recruteurId
    } yield (a.codeROME, a.codeSecteurActivite, a.codeDepartement, a.frequence, a.alerteId)
  }

  // FIXME : logique dupliquée en front
  def alertesParRecruteur(query: AlertesRecruteurQuery): Future[List[AlerteDto]] =
    database.run(alertesParRecruteurQuery(query.recruteurId).result).map(_.toList.map { a =>
      AlerteDto(
        alerteId = a._5.value,
        intitule =
          s"${if (a._1.isDefined) {
            a._1.flatMap(c => rechercheCandidatService.metierProposeParCode(c)).map(_.label).getOrElse("")
          } else if (a._2.isDefined) {
            a._2.map(c => rechercheCandidatService.secteurActiviteParCode(c).label).getOrElse("")
          } else
            "Candidats"
          }${a._3.map(c => s" en ${rechercheCandidatService.departementParCode(c).label}").getOrElse("")}",
        criteres = Criteres(
          codeSecteurActivite = a._2.map(_.value).getOrElse(""),
          codeROME = a._1.map(_.value).getOrElse(""),
          codeDepartement = a._3.map(_.value).getOrElse("")
        ),
        frequence = FrequenceAlerte.label(a._4)
      )
    })

  def alertesQuotidiennes: Source[AlerteRecruteurDto, NotUsed] =
    streamAlertes(FrequenceAlerte.QUOTIDIENNE)

  def alertesHebdomaraires: Source[AlerteRecruteurDto, NotUsed] =
    streamAlertes(FrequenceAlerte.HEBDOMADAIRE)

  def onAlerteRecruteurCreeEvent(event: AlerteRecruteurCreeEvent): Future[Unit] =
    database
      .run(alertesRecruteursTable.map(a => (a.recruteurId, a.prenomRecruteur, a.typeRecruteur, a.emailRecruteur, a.alerteId, a.frequence, a.codeROME, a.codeSecteurActivite, a.codeDepartement))
        += (event.recruteurId, event.prenom, event.typeRecruteur, event.email, event.alerteId, event.frequence, event.codeROME, event.codeSecteurActivite, event.codeDepartement))
      .map(_ => ())

  def onAlerteRecruteurSupprimeeEvent(event: AlerteRecruteurSupprimeeEvent): Future[Unit] =
    database.run(alertesParIdQuery(event.alerteId).delete).map(_ => ())

  def onProfilModifieEvent(event: ProfilModifieEvent): Future[Unit] =
    database.run(modifierProfilQuery(event.recruteurId).update(
      event.typeRecruteur
    )).map(_ => ())

  def onProfilGerantModifieEvent(event: ProfilGerantModifieEvent): Future[Unit] =
    database.run(modifierProfilGerantQuery(event.recruteurId).update((
      event.prenom,
      event.email
    ))).map(_ => ())

  private def streamAlertes(frequenceAlerte: FrequenceAlerte): Source[AlerteRecruteurDto, NotUsed] =
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

  private def toAlerteRecruteurDto(alerteRecruteurRecord: AlerteRecruteurRecord): AlerteRecruteurDto = {
    alerteRecruteurRecord.codeROME.map(codeROME =>
      AlerteRecruteurMetierDto(
        recruteurId = alerteRecruteurRecord.recruteurId,
        typeRecruteur = alerteRecruteurRecord.typeRecruteur,
        prenom = alerteRecruteurRecord.prenomRecruteur,
        email = alerteRecruteurRecord.emailRecruteur,
        alerteId = alerteRecruteurRecord.alerteId,
        frequence = alerteRecruteurRecord.frequence,
        metier = rechercheCandidatService.metierProposeParCode(codeROME).get,
        departement = alerteRecruteurRecord.codeDepartement.map(rechercheCandidatService.departementParCode)
      )
    ).orElse(alerteRecruteurRecord.codeSecteurActivite.map(codeSecteurActivite =>
      AlerteRecruteurSecteurDto(
        recruteurId = alerteRecruteurRecord.recruteurId,
        typeRecruteur = alerteRecruteurRecord.typeRecruteur,
        prenom = alerteRecruteurRecord.prenomRecruteur,
        email = alerteRecruteurRecord.emailRecruteur,
        alerteId = alerteRecruteurRecord.alerteId,
        frequence = alerteRecruteurRecord.frequence,
        secteurActivite = rechercheCandidatService.secteurActiviteParCode(codeSecteurActivite),
        departement = alerteRecruteurRecord.codeDepartement.map(rechercheCandidatService.departementParCode)
      )
    )).orElse(alerteRecruteurRecord.codeDepartement.map(codeDepartement =>
      AlerteRecruteurDepartementDto(
        recruteurId = alerteRecruteurRecord.recruteurId,
        typeRecruteur = alerteRecruteurRecord.typeRecruteur,
        prenom = alerteRecruteurRecord.prenomRecruteur,
        email = alerteRecruteurRecord.emailRecruteur,
        alerteId = alerteRecruteurRecord.alerteId,
        frequence = alerteRecruteurRecord.frequence,
        departement = rechercheCandidatService.departementParCode(codeDepartement)
      )
    )).getOrElse(throw new IllegalArgumentException("Type d'alerte non géré"))
  }
}
