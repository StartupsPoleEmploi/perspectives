package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.candidat._
import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.recruteur.TypeRecruteur
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatProjection(val driver: PostgresDriver,
                         database: Database) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscrisEvent => onCandidatInscrisEvent(e)
    case e: ProfilCandidatModifiePEConnectEvent => onProfilPEConnectModifieEvent(e)
    case e: CriteresRechercheModifiesEvent => onCriteresRechercheModifiesEvent(e)
    case e: NumeroTelephoneModifieEvent => onNumeroTelephoneModifieEvent(e)
    case e: AdressePEConnectModifieeEvent => Future.successful(())
    case e: StatutDemandeurEmploiPEConnectModifieEvent => onStatutDemandeurEmploiPEConnectModifieEvent(e)
    case e: CVAjouteEvent => onCVAjouteEvent(e)
    case e: CVRemplaceEvent => onCVRemplaceEvent(e)
  }

  import driver.api._

  class CandidatTable(tag: Tag) extends Table[CandidatDto](tag, "candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def genre = column[Option[Genre]]("genre")

    def email = column[String]("email")

    def statutDemandeurEmploi = column[Option[StatutDemandeurEmploi]]("statut_demandeur_emploi")

    def rechercheMetierEvalue = column[Option[Boolean]]("recherche_metier_evalue")

    def metiersEvalues = column[List[Metier]]("metiers_evalues")

    def rechercheAutreMetier = column[Option[Boolean]]("recherche_autre_metier")

    def metiersRecherches = column[List[Metier]]("metiers_recherches")

    def contacteParAgenceInterim = column[Option[Boolean]]("contacte_par_agence_interim")

    def contacteParOrganismeFormation = column[Option[Boolean]]("contacte_par_organisme_formation")

    def rayonRecherche = column[Option[RayonRecherche]]("rayon_recherche")

    def numeroTelephone = column[Option[NumeroTelephone]]("numero_telephone")

    def cvId = column[Option[CVId]]("cv_id")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def * = (candidatId, nom, prenom, genre, email, statutDemandeurEmploi, rechercheMetierEvalue, metiersEvalues, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche, numeroTelephone, cvId, dateInscription) <> (CandidatDto.tupled, CandidatDto.unapply)
  }

  val candidatTable = TableQuery[CandidatTable]

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] = {
    val select = candidatTable.filter(c => c.candidatId === query.candidatId)

    database.run(select.result.head)
  }

  def findAllOrderByDateInscription: Future[List[CandidatDto]] = {
    val select = candidatTable.sortBy(_.dateInscription.desc)

    database.run(select.result).map(_.toList)
  }

  def rechercherCandidatParDateInscription(query: RechercherCandidatsParDateInscriptionQuery): Future[ResultatRechercheCandidatParDateInscription] =
    database.run(
      candidatTable
        .filter(c => filtreCandidatAvecCriteresDeRecherche(c) && filtreTypeRecruteur(c, query.typeRecruteur))
        .sortBy(_.dateInscription.desc).result
    ).map(r => ResultatRechercheCandidatParDateInscription(ListeCandidats(
      nbCandidats = r.size, // FIXME : count avec pagination
      candidatDtos = r.toList
    )))

  def rechercherCandidatParSecteur(query: RechercheCandidatsParSecteurQuery): Future[ResultatRechercheCandidatParSecteur] =
    rechercherCandidats(query.typeRecruteur, query.secteur.metiers)
      .map(r => {
        ResultatRechercheCandidatParSecteur(
          validesSecteur = ListeCandidats(
            nbCandidats = r._1.size,
            candidatDtos = r._1
          ),
          interessesSecteur = ListeCandidats(
            nbCandidats = r._2.size,
            candidatDtos = r._2
          )
        )
      })

  def rechercherCandidatParMetier(query: RechercherCandidatsParMetierQuery): Future[ResultatRechercheCandidatParMetier] =
    rechercherCandidats(query.typeRecruteur, query.metiers)
      .map(r => {
        ResultatRechercheCandidatParMetier(
          validesMetier = ListeCandidats(
            nbCandidats = r._1.size,
            candidatDtos = r._1
          ),
          interessesMetier = ListeCandidats(
            nbCandidats = r._2.size,
            candidatDtos = r._2
          )
        )
      })

  private def filtreCandidatAvecCriteresDeRecherche(c: CandidatTable): Rep[Boolean] =
    c.rechercheMetierEvalue.isDefined && c.rechercheAutreMetier.isDefined

  private def filtreTypeRecruteur(c: CandidatTable,
                                  typeRecruteur: TypeRecruteur): Rep[Option[Boolean]] = typeRecruteur match {
    case TypeRecruteur.AGENCE_INTERIM => c.contacteParAgenceInterim === true
    case TypeRecruteur.ORGANISME_FORMATION => c.contacteParOrganismeFormation === true
    case _ => Some(true)
  }

  // FIXME : count avec pagination
  private def rechercherCandidats(typeRecruteur: TypeRecruteur, metiers: Set[Metier]): Future[(List[CandidatDto], List[CandidatDto])] = {
    val listeMetiers = metiers.toList

    val selectMetiersEvalues = candidatTable.filter { c =>
      filtreTypeRecruteur(c, typeRecruteur) &&
        c.rechercheMetierEvalue === true &&
        c.metiersEvalues @& listeMetiers
    }.sortBy(_.candidatId)
    val selectMetiersRecherches = candidatTable.filter { c =>
      filtreTypeRecruteur(c, typeRecruteur) &&
        c.rechercheAutreMetier === true &&
        c.metiersRecherches @& listeMetiers &&
        !(c.metiersEvalues @& listeMetiers)
    }.sortBy(_.candidatId)

    // La deuxième liste ne contiendra pas de doublons  par rapport à la première car ils sont exclus par la requete
    for {
      candidatsValides <- database.run(selectMetiersEvalues.result)
      candidatsInteresses <- database.run(selectMetiersRecherches.result)
    } yield (candidatsValides.toList, candidatsInteresses.toList)
  }

  private def onCandidatInscrisEvent(event: CandidatInscrisEvent): Future[Unit] =
    database
      .run(candidatTable.map(c => (c.candidatId, c.nom, c.prenom, c.genre, c.email, c.dateInscription))
        += (event.candidatId, event.nom, event.prenom, event.genre, event.email, event.date))
      .map(_ => ())

  private def onProfilPEConnectModifieEvent(event: ProfilCandidatModifiePEConnectEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === event.candidatId
    } yield (c.nom, c.prenom, c.email, c.genre)
    val updateAction = query.update((
      event.nom,
      event.prenom,
      event.email,
      Some(event.genre)
    ))

    database.run(updateAction).map(_ => ())
  }

  private def onCriteresRechercheModifiesEvent(event: CriteresRechercheModifiesEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === event.candidatId
    } yield (c.contacteParOrganismeFormation, c.contacteParAgenceInterim, c.rechercheMetierEvalue, c.rechercheAutreMetier, c.rayonRecherche, c.metiersRecherches)
    val updateAction = query.update((
      Some(event.etreContacteParOrganismeFormation),
      Some(event.etreContacteParAgenceInterim),
      Some(event.rechercheMetierEvalue),
      Some(event.rechercheAutreMetier),
      Some(event.rayonRecherche),
      event.metiersRecherches.toList
    ))

    database.run(updateAction).map(_ => ())
  }

  private def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === event.candidatId
    } yield c.numeroTelephone
    val updateAction = query.update(Some(event.numeroTelephone))

    database.run(updateAction).map(_ => ())
  }

  private def onStatutDemandeurEmploiPEConnectModifieEvent(event: StatutDemandeurEmploiPEConnectModifieEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === event.candidatId
    } yield c.statutDemandeurEmploi
    val updateAction = query.update(Some(event.statutDemandeurEmploi))

    database.run(updateAction).map(_ => ())
  }

  private def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === event.candidatId
    } yield c.cvId
    val updateAction = query.update(Some(event.cvId))

    database.run(updateAction).map(_ => ())
  }

  private def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] = {
    val query = for {
      c <- candidatTable if c.candidatId === event.candidatId
    } yield c.cvId
    val updateAction = query.update(Some(event.cvId))

    database.run(updateAction).map(_ => ())
  }

}
