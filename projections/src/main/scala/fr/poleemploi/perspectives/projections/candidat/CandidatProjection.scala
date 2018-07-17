package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.{AggregateId, Event}
import fr.poleemploi.perspectives.domain.candidat._
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone}
import fr.poleemploi.perspectives.projections.infra.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatDto(candidatId: String,
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
    case e: CandidatInscrisEvent => onCandidatInscrisEvent(aggregateId, e)
    case e: ProfilCandidatModifiePEConnectEvent => onProfilPEConnectModifieEvent(aggregateId, e)
    case e: CriteresRechercheModifiesEvent => onCriteresRechercheModifiesEvent(aggregateId, e)
    case e: NumeroTelephoneModifieEvent => onNumeroTelephoneModifieEvent(aggregateId, e)
  }

  import driver.api._

  class CandidatTable(tag: Tag) extends Table[CandidatRecord](tag, "candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[String]("candidat_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def genre = column[Option[String]]("genre")

    def email = column[String]("email")

    def rechercheMetierEvalue = column[Option[Boolean]]("recherche_metier_evalue")

    def rechercheAutreMetier = column[Option[Boolean]]("recherche_autre_metier")

    def metiersRecherches = column[List[String]]("metiers_recherches")

    def contacteParAgenceInterim = column[Option[Boolean]]("contacte_par_agence_interim")

    def contacteParOrganismeFormation = column[Option[Boolean]]("contacte_par_organisme_formation")

    def rayonRecherche = column[Option[Int]]("rayon_recherche")

    def numeroTelephone = column[Option[String]]("numero_telephone")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def * = (candidatId, nom, prenom, genre, email, rechercheMetierEvalue, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche, numeroTelephone, dateInscription) <> (CandidatRecord.tupled, CandidatRecord.unapply)
  }

  val candidatTable = TableQuery[CandidatTable]

  def getCandidat(candidatId: String): Future[CandidatDto] = {
    val query = candidatTable.filter(c => c.candidatId === candidatId)

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
        c => (c.candidatId, c.nom, c.prenom, c.genre, c.email, c.metiersRecherches, c.dateInscription))
        += (aggregateId.value, event.nom, event.prenom, event.genre, event.email, Nil, event.date))
      .map(_ => ())

  private def onProfilPEConnectModifieEvent(aggregateId: AggregateId,
                                           event: ProfilCandidatModifiePEConnectEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === aggregateId.value
    } yield (c.nom, c.prenom, c.email, c.genre)
    val updateAction = query.update((
      event.nom,
      event.prenom,
      event.email,
      Some(event.genre)
    ))

    database.run(updateAction).map(_ => ())
  }

  private def onCriteresRechercheModifiesEvent(aggregateId: AggregateId,
                                               event: CriteresRechercheModifiesEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === aggregateId.value
    } yield (c.contacteParOrganismeFormation, c.contacteParAgenceInterim, c.rechercheMetierEvalue, c.rechercheAutreMetier, c.rayonRecherche, c.metiersRecherches)
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

  private def onNumeroTelephoneModifieEvent(aggregateId: AggregateId,
                                            event: NumeroTelephoneModifieEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === aggregateId.value
    } yield c.numeroTelephone
    val updateAction = query.update(Some(event.numeroTelephone))

    database.run(updateAction).map(_ => ())
  }

}

private[candidat] case class CandidatRecord(candidatId: String,
                                            nom: String,
                                            prenom: String,
                                            genre: Option[String],
                                            email: String,
                                            rechercheMetierEvalue: Option[Boolean],
                                            rechercheAutreMetier: Option[Boolean],
                                            metiersRecherches: List[String],
                                            contacteParAgenceInterim: Option[Boolean],
                                            contacteParOrganismeFormation: Option[Boolean],
                                            rayonRecherche: Option[Int],
                                            numeroTelephone: Option[String],
                                            dateInscription: ZonedDateTime) {

  def toCandidatDto: CandidatDto = CandidatDto(
    candidatId = candidatId,
    nom = nom,
    prenom = prenom,
    genre = genre.flatMap(Genre.from),
    email = email,
    rechercheAutreMetier = rechercheAutreMetier,
    rechercheMetierEvalue = rechercheMetierEvalue,
    metiersRecherches = metiersRecherches.flatMap(Metier.from).toSet,
    contacteParAgenceInterim = contacteParAgenceInterim,
    contacteParOrganismeFormation = contacteParOrganismeFormation,
    rayonRecherche = rayonRecherche,
    numeroTelephone = numeroTelephone.flatMap(NumeroTelephone.from),
    dateInscription = dateInscription
  )
}
