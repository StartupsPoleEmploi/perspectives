package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatProjection(val driver: PostgresDriver,
                         database: Database,
                         candidatsTesteurs: List[CandidatId]) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
    case e: ProfilCandidatModifieEvent => onProfilModifieEvent(e)
    case e: CriteresRechercheModifiesEvent => onCriteresRechercheModifiesEvent(e)
    case e: NumeroTelephoneModifieEvent => onNumeroTelephoneModifieEvent(e)
    case e: AdresseModifieeEvent => onAdresseModifieeEvent(e)
    case e: StatutDemandeurEmploiModifieEvent => onStatutDemandeurEmploiModifieEvent(e)
    case e: CVAjouteEvent => onCVAjouteEvent(e)
    case e: CVRemplaceEvent => onCVRemplaceEvent(e)
    case e: MRSAjouteeEvent => onMRSAjouteeEvent(e)
    case e: RepriseEmploiDeclareeParConseillerEvent => onRepriseEmploiDeclareeParConseillerEvent(e)
  }

  import driver.api._

  class CandidatTable(tag: Tag) extends Table[CandidatDto](tag, "candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def genre = column[Genre]("genre")

    def email = column[String]("email")

    def statutDemandeurEmploi = column[Option[StatutDemandeurEmploi]]("statut_demandeur_emploi")

    def codePostal = column[Option[String]]("code_postal")

    def commune = column[Option[String]]("commune")

    def rechercheMetierEvalue = column[Option[Boolean]]("recherche_metier_evalue")

    def metiersEvalues = column[List[CodeROME]]("metiers_evalues")

    def rechercheAutreMetier = column[Option[Boolean]]("recherche_autre_metier")

    def metiersRecherches = column[List[CodeROME]]("metiers_recherches")

    def contacteParAgenceInterim = column[Option[Boolean]]("contacte_par_agence_interim")

    def contacteParOrganismeFormation = column[Option[Boolean]]("contacte_par_organisme_formation")

    def rayonRecherche = column[Option[RayonRecherche]]("rayon_recherche")

    def numeroTelephone = column[Option[NumeroTelephone]]("numero_telephone")

    def cvId = column[Option[CVId]]("cv_id")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def indexerMatching = column[Boolean]("indexer_matching")

    def * = (candidatId, nom, prenom, genre, email, statutDemandeurEmploi, codePostal, commune, rechercheMetierEvalue, metiersEvalues, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche, numeroTelephone, cvId, dateInscription, indexerMatching) <> (CandidatDto.tupled, CandidatDto.unapply)
  }

  val candidatTable = TableQuery[CandidatTable]
  val getCandidatQuery = Compiled { candidatId: Rep[CandidatId] =>
    candidatTable.filter(c => c.candidatId === candidatId)
  }
  val listerParDateInscriptionQuery = candidatTable.sortBy(_.dateInscription.desc)
  val modifierProfilQuery = Compiled { candidatId: Rep[CandidatId] =>
    for {
      c <- candidatTable if c.candidatId === candidatId
    } yield (c.nom, c.prenom, c.email, c.genre)
  }
  val modifierCriteresRechercheQuery = Compiled { candidatId: Rep[CandidatId] =>
    for {
      c <- candidatTable if c.candidatId === candidatId
    } yield (c.contacteParOrganismeFormation, c.contacteParAgenceInterim, c.rechercheMetierEvalue, c.rechercheAutreMetier, c.rayonRecherche, c.metiersRecherches, c.indexerMatching)
  }
  val modifierNumeroTelephoneQuery = Compiled { candidatId: Rep[CandidatId] =>
    for {
      c <- candidatTable if c.candidatId === candidatId
    } yield c.numeroTelephone
  }
  val modifierStatutDemandeurEmploiQuery = Compiled { candidatId: Rep[CandidatId] =>
    for {
      c <- candidatTable if c.candidatId === candidatId
    } yield c.statutDemandeurEmploi
  }
  val modifierCVQuery = Compiled { candidatId: Rep[CandidatId] =>
    for {
      c <- candidatTable if c.candidatId === candidatId
    } yield c.cvId
  }
  val modifierAdresseQuery = Compiled { candidatId: Rep[CandidatId] =>
    for {
      c <- candidatTable if c.candidatId === candidatId
    } yield (c.codePostal, c.commune)
  }
  val repriseEmploiQuery = Compiled { candidatId: Rep[CandidatId] =>
    for {
      c <- candidatTable if c.candidatId === candidatId
    } yield (c.rechercheMetierEvalue, c.rechercheAutreMetier, c.metiersRecherches, c.indexerMatching)
  }

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] =
    database.run(getCandidatQuery(query.candidatId).result.head)

  def listerParDateInscription: Future[List[CandidatDto]] =
    database.run(listerParDateInscriptionQuery.result).map(_.toList)

  def rechercherCandidatParDateInscription(query: RechercherCandidatsParDateInscriptionQuery): Future[ResultatRechercheCandidatParDateInscription] =
    database.run(
      candidatTable
        .filter(c => filtreCandidatAvecCriteresDeRecherche(c) && filtreTypeRecruteur(c, query.typeRecruteur))
        .sortBy(_.dateInscription.desc).result
    ).map(r => ResultatRechercheCandidatParDateInscription(r.toList))

  def rechercherCandidatParSecteur(query: RechercherCandidatsParSecteurQuery): Future[ResultatRechercheCandidatParSecteur] = {
    val metiersSecteur = SecteurActivite.parCode(query.codeSecteurActivite).metiers.map(_.codeROME)

    // Candidats qui recherchent parmis leurs métiers évalués et qui ont été évalués sur un métier du secteur
    val selectCandidatsEvaluesSurSecteur = candidatTable.filter { c =>
      filtreTypeRecruteur(c, query.typeRecruteur) &&
        c.numeroTelephone.isDefined &&
        c.indexerMatching &&
        c.rechercheMetierEvalue === true &&
        c.metiersEvalues @& metiersSecteur
    }.sortBy(_.dateInscription)

    // Candidats qui sont intéréssés par un metier du secteur et qui ont été évalués sur un metier d'un autre secteur
    val selectCandidatsInteressesParAutreSecteur = candidatTable.filter { c =>
      filtreTypeRecruteur(c, query.typeRecruteur) &&
        c.numeroTelephone.isDefined &&
        c.indexerMatching &&
        c.rechercheAutreMetier === true &&
        c.metiersRecherches @& metiersSecteur &&
        !(c.metiersEvalues @& metiersSecteur)
    }.sortBy(_.dateInscription)

    for {
      candidatsEvaluesSurSecteur <- database.run(selectCandidatsEvaluesSurSecteur.result)
      candidatsInteressesParAutreSecteur <- database.run(selectCandidatsInteressesParAutreSecteur.result)
    } yield
      ResultatRechercheCandidatParSecteur(
        candidatsEvaluesSurSecteur = candidatsEvaluesSurSecteur.toList,
        candidatsInteressesParAutreSecteur = candidatsInteressesParAutreSecteur.toList
      )
  }

  def rechercherCandidatParMetier(query: RechercherCandidatsParMetierQuery): Future[ResultatRechercheCandidatParMetier] = {
    val metiers = List(query.codeROME)

    // Candidats qui recherchent parmis leurs métiers évalués et qui ont été évalués sur le métier
    val selectCandidatsEvaluesSurMetier = candidatTable.filter { c =>
      filtreTypeRecruteur(c, query.typeRecruteur) &&
        c.numeroTelephone.isDefined &&
        c.indexerMatching &&
        c.rechercheMetierEvalue === true &&
        c.metiersEvalues @& metiers
    }.sortBy(_.dateInscription)

    // Candidats qui sont intéréssés par ce métier et qui ont été évalués sur un métier du meme secteur
    val metiersSecteur = SecteurActivite.parMetier(query.codeROME).metiers.map(_.codeROME)
    val metiersSecteurSansMetierChoisi = metiersSecteur.filter(_ != query.codeROME)
    val selectCandidatsInteressesParMetierMemeSecteur = candidatTable.filter { c =>
      filtreTypeRecruteur(c, query.typeRecruteur) &&
        c.numeroTelephone.isDefined &&
        c.indexerMatching &&
        c.rechercheAutreMetier === true &&
        c.metiersRecherches @& metiers &&
        c.metiersEvalues @& metiersSecteurSansMetierChoisi
    }.sortBy(_.dateInscription)

    // Candidats qui sont intéréssés par ce métier et qui ont été évalués sur un métier d'un autre secteur
    val selectCandidatsInteressesParMetierAutreSecteur = candidatTable.filter { c =>
      filtreTypeRecruteur(c, query.typeRecruteur) &&
        c.numeroTelephone.isDefined &&
        c.indexerMatching &&
        c.rechercheAutreMetier === true &&
        c.metiersRecherches @& metiers &&
        !(c.metiersEvalues @& metiersSecteur)
    }.sortBy(_.dateInscription)

    for {
      candidatsEvaluesSurMetier <- database.run(selectCandidatsEvaluesSurMetier.result)
      candidatsInteressesParMetierMemeSecteur <- database.run(selectCandidatsInteressesParMetierMemeSecteur.result)
      candidatsInteressesParMetierAutreSecteur <- database.run(selectCandidatsInteressesParMetierAutreSecteur.result)
    } yield
      ResultatRechercheCandidatParMetier(
        candidatsEvaluesSurMetier = candidatsEvaluesSurMetier.toList,
        candidatsInteressesParMetierMemeSecteur = candidatsInteressesParMetierMemeSecteur.toList,
        candidatsInteressesParMetierAutreSecteur = candidatsInteressesParMetierAutreSecteur.toList
      )
  }

  private def filtreCandidatAvecCriteresDeRecherche(c: CandidatTable): Rep[Boolean] =
  // FIXME : faire en une seule condition + modifier toutes les requetes de matching une fois que la projection peut traiter les evenements en séquentiel pour un même candidat (en parallèle sinon)
    (c.rechercheMetierEvalue.isDefined || c.rechercheAutreMetier.isDefined) && c.numeroTelephone.isDefined && c.indexerMatching

  private def filtreTypeRecruteur(c: CandidatTable,
                                  typeRecruteur: TypeRecruteur): Rep[Option[Boolean]] = typeRecruteur match {
    case TypeRecruteur.AGENCE_INTERIM => c.contacteParAgenceInterim === true
    case TypeRecruteur.ORGANISME_FORMATION => c.contacteParOrganismeFormation === true
    case _ => Some(true)
  }

  private def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    database
      .run(candidatTable.map(c => (c.candidatId, c.nom, c.prenom, c.genre, c.email, c.dateInscription))
        += (event.candidatId, event.nom, event.prenom, event.genre, event.email, event.date))
      .map(_ => ())

  private def onProfilModifieEvent(event: ProfilCandidatModifieEvent): Future[Unit] =
    database.run(modifierProfilQuery(event.candidatId).update((
      event.nom,
      event.prenom,
      event.email,
      event.genre
    ))).map(_ => ())

  private def onCriteresRechercheModifiesEvent(event: CriteresRechercheModifiesEvent): Future[Unit] =
    database.run(modifierCriteresRechercheQuery(event.candidatId).update((
      Some(event.etreContacteParOrganismeFormation),
      Some(event.etreContacteParAgenceInterim),
      Some(event.rechercheMetierEvalue),
      Some(event.rechercheAutreMetier),
      Some(event.rayonRecherche),
      event.metiersRecherches.toList,
      !candidatsTesteurs.contains(event.candidatId) && (event.rechercheMetierEvalue || event.rechercheAutreMetier)
    ))).map(_ => ())

  private def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit] =
    database.run(modifierNumeroTelephoneQuery(event.candidatId).update(
      Some(event.numeroTelephone)
    )).map(_ => ())

  private def onStatutDemandeurEmploiModifieEvent(event: StatutDemandeurEmploiModifieEvent): Future[Unit] =
    database.run(modifierStatutDemandeurEmploiQuery(event.candidatId).update(
      Some(event.statutDemandeurEmploi)
    )).map(_ => ())

  private def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] =
    database.run(modifierCVQuery(event.candidatId).update(
      Some(event.cvId)
    )).map(_ => ())

  private def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] =
    database.run(modifierCVQuery(event.candidatId).update(
      Some(event.cvId)
    )).map(_ => ())

  private def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit] =
    database.run(modifierAdresseQuery(event.candidatId).update((
      Some(event.adresse.codePostal),
      Some(event.adresse.libelleCommune)
    ))).map(_ => ())

  private def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit] =
  //FIXME : est faite en une seule requete car la base va gérer le fait que deux evenements peuvent arriver très proches et que la projection n'attend pas la fin du traitement d'un event avant de passer à l'autre
    database.run(
      sqlu"""
            UPDATE candidats
            SET metiers_evalues = ${event.metier.value}::text || metiers_evalues
            WHERE candidat_id = ${event.candidatId.value}
          """).map(_ => ())

  private def onRepriseEmploiDeclareeParConseillerEvent(event: RepriseEmploiDeclareeParConseillerEvent): Future[Unit] =
    database.run(repriseEmploiQuery(event.candidatId).update((
      Some(false),
      Some(false),
      Nil,
      false
    ))).map(_ => ())

}
