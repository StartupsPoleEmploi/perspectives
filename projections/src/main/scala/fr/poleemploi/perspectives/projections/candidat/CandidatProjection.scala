package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.{AggregateId, Event}
import fr.poleemploi.perspectives.domain.candidat._
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone}
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatDto(candidatId: CandidatId,
                       nom: String,
                       prenom: String,
                       genre: Option[Genre],
                       email: String,
                       rechercheMetierEvalue: Option[Boolean],
                       rechercheAutreMetier: Option[Boolean],
                       metiersRecherches: Set[Metier],
                       contacteParAgenceInterim: Option[Boolean],
                       contacteParOrganismeFormation: Option[Boolean],
                       rayonRecherche: Option[Int],
                       numeroTelephone: Option[NumeroTelephone],
                       dateInscription: ZonedDateTime)

class CandidatProjection(val driver: PostgresDriver,
                         database: Database) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatEvent])

  override def isReplayable: Boolean = true

  override def onEvent(aggregateId: AggregateId): ReceiveEvent = {
    // TODO
    case e: CandidatInscrisEvent => onCandidatInscrisEvent(CandidatId(aggregateId.value), e)
    case e: ProfilCandidatModifiePEConnectEvent => onProfilPEConnectModifieEvent(CandidatId(aggregateId.value), e)
    case e: CriteresRechercheModifiesEvent => onCriteresRechercheModifiesEvent(CandidatId(aggregateId.value), e)
    case e: NumeroTelephoneModifieEvent => onNumeroTelephoneModifieEvent(CandidatId(aggregateId.value), e)
  }

  import driver.api._

  class CandidatTable(tag: Tag) extends Table[CandidatDto](tag, "candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def genre = column[Option[Genre]]("genre")

    def email = column[String]("email")

    def rechercheMetierEvalue = column[Option[Boolean]]("recherche_metier_evalue")

    def rechercheAutreMetier = column[Option[Boolean]]("recherche_autre_metier")

    def metiersRecherches = column[Set[Metier]]("metiers_recherches")

    def contacteParAgenceInterim = column[Option[Boolean]]("contacte_par_agence_interim")

    def contacteParOrganismeFormation = column[Option[Boolean]]("contacte_par_organisme_formation")

    def rayonRecherche = column[Option[Int]]("rayon_recherche")

    def numeroTelephone = column[Option[NumeroTelephone]]("numero_telephone")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def * = (candidatId, nom, prenom, genre, email, rechercheMetierEvalue, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche, numeroTelephone, dateInscription) <> (CandidatDto.tupled, CandidatDto.unapply)
  }

  val candidatTable = TableQuery[CandidatTable]

  def getCandidat(candidatId: CandidatId): Future[CandidatDto] = {
    val query = candidatTable.filter(c => c.candidatId === candidatId)

    database.run(query.result.head)
  }

  def findAllOrderByDateInscription: Future[List[CandidatDto]] = {
    val query = candidatTable.sortBy(_.dateInscription.desc)

    database.run(query.result).map(_.toList)
  }

  private def onCandidatInscrisEvent(candidatId: CandidatId,
                                     event: CandidatInscrisEvent): Future[Unit] =
    database
      .run(candidatTable.map(
        c => (c.candidatId, c.nom, c.prenom, c.genre, c.email, c.metiersRecherches, c.dateInscription))
        += (candidatId, event.nom, event.prenom, event.genre, event.email, Set.empty, event.date))
      .map(_ => ())

  private def onProfilPEConnectModifieEvent(candidatId: CandidatId,
                                            event: ProfilCandidatModifiePEConnectEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === candidatId
    } yield (c.nom, c.prenom, c.email, c.genre)
    val updateAction = query.update((
      event.nom,
      event.prenom,
      event.email,
      Some(event.genre)
    ))

    database.run(updateAction).map(_ => ())
  }

  private def onCriteresRechercheModifiesEvent(candidatId: CandidatId,
                                               event: CriteresRechercheModifiesEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === candidatId
    } yield (c.contacteParOrganismeFormation, c.contacteParAgenceInterim, c.rechercheMetierEvalue, c.rechercheAutreMetier, c.rayonRecherche, c.metiersRecherches)
    val updateAction = query.update((
      Some(event.etreContacteParOrganismeFormation),
      Some(event.etreContacteParAgenceInterim),
      Some(event.rechercheMetierEvalue),
      Some(event.rechercheAutreMetier),
      Some(event.rayonRecherche),
      event.listeMetiersRecherches
    ))

    database.run(updateAction).map(_ => ())
  }

  private def onNumeroTelephoneModifieEvent(candidatId: CandidatId,
                                            event: NumeroTelephoneModifieEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === candidatId
    } yield c.numeroTelephone
    val updateAction = query.update(Some(event.numeroTelephone))

    database.run(updateAction).map(_ => ())
  }

}
