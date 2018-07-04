package fr.poleemploi.perspectives.projections

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.{AggregateId, Event}
import fr.poleemploi.perspectives.domain.Metier
import fr.poleemploi.perspectives.domain.candidat.{CandidatInscrisEvent, CriteresRechercheModifiesEvent}
import fr.poleemploi.perspectives.projections.infra.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatDto(candidatId: String,
                       peConnectId: String,
                       nom: String,
                       prenom: String,
                       email: String,
                       rechercheMetierEvalue: Option[Boolean],
                       rechercheAutreMetier: Option[Boolean],
                       metiersRecherches: Set[Metier],
                       contacteParAgenceInterim: Option[Boolean],
                       contacteParOrganismeFormation: Option[Boolean],
                       rayonRecherche: Option[Int],
                       dateInscription: ZonedDateTime)

class CandidatProjection(val driver: PostgresDriver,
                         database: Database) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[Event])

  override def isReplayable: Boolean = true

  override def onEvent(aggregateId: AggregateId): ReceiveEvent = {
    case e: CandidatInscrisEvent =>
      onCandidatInscrisEvent(
        aggregateId = aggregateId,
        event = e
      )
    case e: CriteresRechercheModifiesEvent =>
      onCriteresRechercheModifiesEvent(
        aggregateId = aggregateId,
        event = e
      )
  }

  import driver.api._

  class CandidatTable(tag: Tag) extends Table[CandidatRecord](tag, "candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[String]("candidat_id")

    def peConnectId = column[String]("peconnect_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def email = column[String]("email")

    def rechercheMetierEvalue = column[Option[Boolean]]("recherche_metier_evalue")

    def rechercheAutreMetier = column[Option[Boolean]]("recherche_autre_metier")

    def metiersRecherches = column[List[String]]("metiers_recherches")

    def contacteParAgenceInterim = column[Option[Boolean]]("contacte_par_agence_interim")

    def contacteParOrganismeFormation = column[Option[Boolean]]("contacte_par_organisme_formation")

    def rayonRecherche = column[Option[Int]]("rayon_recherche")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def * = (candidatId, peConnectId, nom, prenom, email, rechercheMetierEvalue, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche, dateInscription) <> (CandidatRecord.tupled, CandidatRecord.unapply)
  }

  val candidatTable = TableQuery[CandidatTable]

  def findCandidat(peConnectId: String): Future[Option[CandidatDto]] = {
    val query = candidatTable.filter(u => u.peConnectId === peConnectId)

    database.run(query.result.headOption).map(_.map(_.toCandidatDto))
  }

  def getCandidat(candidatId: String): Future[CandidatDto] = {
    val query = candidatTable.filter(u => u.candidatId === candidatId)

    database.run(query.result.head).map(_.toCandidatDto)
  }

  def findAllOrderByDateInscription: Future[List[CandidatDto]] = {
    val query = candidatTable.sortBy(_.dateInscription.desc)

    database.run(query.result).map(_.toList.map(_.toCandidatDto))
  }

  private def onCandidatInscrisEvent(aggregateId: AggregateId,
                                     event: CandidatInscrisEvent): Future[Unit] =
    database
      .run(candidatTable.map(
        u => (u.candidatId, u.peConnectId, u.nom, u.prenom, u.email, u.metiersRecherches, u.dateInscription))
        += (aggregateId.value, event.peConnectId, event.nom, event.prenom, event.email, Nil, event.date))
      .map(_ => ())

  private def onCriteresRechercheModifiesEvent(aggregateId: AggregateId,
                                               event: CriteresRechercheModifiesEvent): Future[Unit] = {
    val query = for {
      u <- candidatTable if u.candidatId === aggregateId.value
    } yield (u.contacteParOrganismeFormation, u.contacteParAgenceInterim, u.rechercheMetierEvalue, u.rechercheAutreMetier, u.rayonRecherche, u.metiersRecherches)
    val updateAction = query.update((
      Some(event.etreContacteParOrganismeFormation),
      Some(event.etreContacteParAgenceInterim),
      Some(event.rechercheMetierEvalue),
      Some(event.rechercheAutreMetier),
      Some(event.rayonRecherche),
      event.listeMetiersRecherches.toList
    ))

    database.run(updateAction).map(_ => ())
  }
}

case class CandidatRecord(candidatId: String,
                          peConnectId: String,
                          nom: String,
                          prenom: String,
                          email: String,
                          rechercheMetierEvalue: Option[Boolean],
                          rechercheAutreMetier: Option[Boolean],
                          metiersRecherches: List[String],
                          contacteParAgenceInterim: Option[Boolean],
                          contacteParOrganismeFormation: Option[Boolean],
                          rayonRecherche: Option[Int],
                          dateInscription: ZonedDateTime) {

  def toCandidatDto: CandidatDto = CandidatDto(
    candidatId = candidatId,
    peConnectId = peConnectId,
    nom = nom,
    prenom = prenom,
    email = email,
    rechercheAutreMetier = rechercheAutreMetier,
    rechercheMetierEvalue = rechercheMetierEvalue,
    metiersRecherches = metiersRecherches.flatMap(Metier.from).toSet,
    contacteParAgenceInterim = contacteParAgenceInterim,
    contacteParOrganismeFormation = contacteParOrganismeFormation,
    rayonRecherche = rayonRecherche,
    dateInscription = dateInscription
  )
}
