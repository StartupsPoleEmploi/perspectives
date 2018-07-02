package fr.poleemploi.perspectives.projections

import fr.poleemploi.eventsourcing.{AggregateId, AppendedEvent, EventHandler}
import fr.poleemploi.perspectives.domain.candidat.{CandidatInscrisEvent, CriteresRechercheModifiesEvent}
import fr.poleemploi.perspectives.projections.infra.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatDto(candidatId: String,
                       peConnectId: String,
                       rechercheMetierEvalue: Option[Boolean],
                       rechercheAutreMetier: Option[Boolean],
                       metiersRecherches: List[String],
                       contacteParAgenceInterim: Option[Boolean],
                       contacteParOrganismeFormation: Option[Boolean],
                       rayonRecherche: Option[Int])

class CandidatProjection(val driver: PostgresDriver,
                         database: Database) extends EventHandler {

  override def handle(appendedEvent: AppendedEvent): Future[Unit] = appendedEvent.eventType match {
    case "CandidatInscrisEvent" =>
      onCandidatInscrisEvent(
        aggregateId = appendedEvent.aggregateId,
        event = appendedEvent.event.asInstanceOf[CandidatInscrisEvent]
      )
    case "CriteresRechercheModifiesEvent" =>
      onCriteresRechercheModifiesEvent(
        aggregateId = appendedEvent.aggregateId,
        event = appendedEvent.event.asInstanceOf[CriteresRechercheModifiesEvent]
      )
    case _ => Future.successful()
  }

  import driver.api._

  class CandidatTable(tag: Tag) extends Table[CandidatDto](tag, "candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[String]("candidat_id")

    def peConnectId = column[String]("peconnect_id")

    def rechercheMetierEvalue = column[Option[Boolean]]("recherche_metier_evalue")

    def rechercheAutreMetier = column[Option[Boolean]]("recherche_autre_metier")

    def metiersRecherches = column[List[String]]("metiers_recherches")

    def contacteParAgenceInterim = column[Option[Boolean]]("contacte_par_agence_interim")

    def contacteParOrganismeFormation = column[Option[Boolean]]("contacte_par_organisme_formation")

    def rayonRecherche = column[Option[Int]]("rayon_recherche")

    def * = (candidatId, peConnectId, rechercheMetierEvalue, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche) <> (CandidatDto.tupled, CandidatDto.unapply)
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

  private def onCandidatInscrisEvent(aggregateId: AggregateId,
                                     event: CandidatInscrisEvent): Future[Unit] =
    database
      .run(candidatTable.map(
        u => (u.candidatId, u.peConnectId, u.metiersRecherches))
        += (aggregateId.value, event.peConnectId, Nil))
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
      event.listeMetiersRecherches
    ))

    database.run(updateAction).map(_ => ())
  }
}
