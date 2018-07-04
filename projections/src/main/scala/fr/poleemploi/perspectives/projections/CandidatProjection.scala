package fr.poleemploi.perspectives.projections

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.{AggregateId, Event}
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
                       metiersRecherches: List[String],
                       contacteParAgenceInterim: Option[Boolean],
                       contacteParOrganismeFormation: Option[Boolean],
                       rayonRecherche: Option[Int])

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

  class CandidatTable(tag: Tag) extends Table[CandidatDto](tag, "candidats") {

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

    def * = (candidatId, peConnectId, nom, prenom, email, rechercheMetierEvalue, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche) <> (CandidatDto.tupled, CandidatDto.unapply)
  }

  val candidatTable = TableQuery[CandidatTable]

  def findCandidat(peConnectId: String): Future[Option[CandidatDto]] = {
    val query = candidatTable.filter(u => u.peConnectId === peConnectId)

    database.run(query.result.headOption)
  }

  def getCandidat(candidatId: String): Future[CandidatDto] = {
    val query = candidatTable.filter(u => u.candidatId === candidatId)

    database.run(query.result.head)
  }

  def findAll: Future[List[CandidatDto]] =
    database.run(candidatTable.result).map(_.toList)

  private def onCandidatInscrisEvent(aggregateId: AggregateId,
                                     event: CandidatInscrisEvent): Future[Unit] =
    database
      .run(candidatTable.map(
        u => (u.candidatId, u.peConnectId, u.nom, u.prenom, u.email, u.metiersRecherches))
        += (aggregateId.value, event.peConnectId, event.nom, event.prenom, event.email, Nil))
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
