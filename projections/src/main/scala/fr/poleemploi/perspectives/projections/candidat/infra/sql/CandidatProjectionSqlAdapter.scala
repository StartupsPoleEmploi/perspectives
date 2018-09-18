package fr.poleemploi.perspectives.projections.candidat.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatProjectionSqlAdapter(database: Database,
                                   candidatsTesteurs: List[CandidatId],
                                   referentielMetier: ReferentielMetier,
                                   rechercheCandidatService: RechercheCandidatService) {

  import PostgresDriver.api._

  class CandidatTable(tag: Tag) extends Table[CandidatRecord](tag, "candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def nom = column[String]("nom")

    def prenom = column[String]("prenom")

    def genre = column[Genre]("genre")

    def email = column[Email]("email")

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

    def cvTypeMedia = column[Option[TypeMedia]]("cv_type_media")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def indexerMatching = column[Boolean]("indexer_matching")

    def * = (candidatId, nom, prenom, genre, email, statutDemandeurEmploi, codePostal, commune, rechercheMetierEvalue, metiersEvalues, rechercheAutreMetier, metiersRecherches, contacteParAgenceInterim, contacteParOrganismeFormation, rayonRecherche, numeroTelephone, cvId, cvTypeMedia, dateInscription, indexerMatching) <> (CandidatRecord.tupled, CandidatRecord.unapply)
  }

  val candidatTable = TableQuery[CandidatTable]

  implicit object CandidatContactRecruteurShape extends CaseClassShape(CandidatContactRecruteurLifted.tupled, CandidatContactRecruteurDto.tupled)

  implicit object CandidatPourConseillerShape extends CaseClassShape(CandidatPourConseillerLifted.tupled, CandidatPourConseillerRecord.tupled)

  implicit object CandidatCriteresRechercheShape extends CaseClassShape(CandidatCriteresRechercheLifted.tupled, CandidatCriteresRechercheDto.tupled)

  implicit object RechercheCandidatShape extends CaseClassShape(RechercheCandidatLifted.tupled, RechercheCandidatRecord.tupled)

  val criteresRechercheQuery = Compiled { candidatId: Rep[CandidatId] =>
    candidatTable
      .filter(_.candidatId === candidatId)
      .map(c => CandidatCriteresRechercheLifted(c.candidatId, c.nom, c.prenom, c.rechercheMetierEvalue, c.rechercheAutreMetier, c.metiersRecherches, c.contacteParAgenceInterim, c.contacteParOrganismeFormation, c.rayonRecherche, c.numeroTelephone, c.cvId, c.cvTypeMedia))
  }
  val candidatContactRecruteurQuery = Compiled { candidatId: Rep[CandidatId] =>
    candidatTable
      .filter(_.candidatId === candidatId)
      .map(c => CandidatContactRecruteurLifted(c.contacteParAgenceInterim, c.contacteParOrganismeFormation))
  }
  val listerParDateInscriptionQuery = Compiled {
    candidatTable
      .sortBy(_.dateInscription.desc)
      .map(c => CandidatPourConseillerLifted(c.candidatId, c.nom, c.prenom, c.genre, c.email, c.statutDemandeurEmploi, c.rechercheMetierEvalue, c.metiersEvalues, c.rechercheAutreMetier, c.metiersRecherches, c.contacteParAgenceInterim, c.contacteParOrganismeFormation, c.rayonRecherche, c.numeroTelephone, c.dateInscription))
  }
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
    } yield (c.cvId, c.cvTypeMedia)
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

  def criteresRecherche(query: CriteresRechercheQuery): Future[CandidatCriteresRechercheDto] =
    database.run(criteresRechercheQuery(query.candidatId).result.head)

  def candidatContactRecruteur(candidatId: CandidatId): Future[CandidatContactRecruteurDto] =
    database.run(candidatContactRecruteurQuery(candidatId).result.head)

  def listerParDateInscriptionPourConseiller: Future[List[CandidatPourConseillerDto]] =
    database.run(listerParDateInscriptionQuery.result)
      .map(_.toList.map(toCandidatPourConseillerDto))

  def rechercherCandidatParDateInscription(query: RechercherCandidatsParDateInscriptionQuery): Future[ResultatRechercheCandidatParDateInscription] =
    database.run(
      candidatTable
        .filter(c => filtreMatching(c, query.typeRecruteur, query.codeDepartement))
        .sortBy(_.dateInscription.desc).map(rechercheCandidatDtoShape).result
    ).map(r => ResultatRechercheCandidatParDateInscription(r.toList.map(toRechercheCandidatDto)))

  def rechercherCandidatParSecteur(query: RechercherCandidatsParSecteurQuery): Future[ResultatRechercheCandidatParSecteur] = {
    val metiersSecteur = rechercheCandidatService.secteurActiviteParCode(query.codeSecteurActivite).metiers.map(_.codeROME)

    // Candidats qui recherchent parmis leurs métiers évalués et qui ont été évalués sur un métier du secteur
    val selectCandidatsEvaluesSurSecteur = candidatTable.filter(c =>
      filtreMatching(c, query.typeRecruteur, query.codeDepartement) &&
        c.rechercheMetierEvalue === true &&
        c.metiersEvalues @& metiersSecteur
    ).sortBy(_.dateInscription).map(rechercheCandidatDtoShape)

    // Candidats qui sont intéréssés par un metier du secteur et qui ont été évalués sur un metier d'un autre secteur
    val selectCandidatsInteressesParAutreSecteur = candidatTable.filter(c =>
      filtreMatching(c, query.typeRecruteur, query.codeDepartement) &&
        c.rechercheAutreMetier === true &&
        c.metiersRecherches @& metiersSecteur &&
        !(c.metiersEvalues @& metiersSecteur)
    ).sortBy(_.dateInscription).map(rechercheCandidatDtoShape)

    for {
      candidatsEvaluesSurSecteur <- database.run(selectCandidatsEvaluesSurSecteur.result)
      candidatsInteressesParAutreSecteur <- database.run(selectCandidatsInteressesParAutreSecteur.result)
    } yield
      ResultatRechercheCandidatParSecteur(
        candidatsEvaluesSurSecteur = candidatsEvaluesSurSecteur.toList.map(toRechercheCandidatDto),
        candidatsInteressesParAutreSecteur = candidatsInteressesParAutreSecteur.toList.map(toRechercheCandidatDto)
      )
  }

  def rechercherCandidatParMetier(query: RechercherCandidatsParMetierQuery): Future[ResultatRechercheCandidatParMetier] = {
    val metiers = List(query.codeROME)

    // Candidats qui recherchent parmis leurs métiers évalués et qui ont été évalués sur le métier
    val selectCandidatsEvaluesSurMetier = candidatTable.filter(c =>
      filtreMatching(c, query.typeRecruteur, query.codeDepartement) &&
        c.rechercheMetierEvalue === true &&
        c.metiersEvalues @& metiers
    ).sortBy(_.dateInscription).map(rechercheCandidatDtoShape)

    // Candidats qui sont intéréssés par ce métier et qui ont été évalués sur un métier du meme secteur
    val metiersSecteur = rechercheCandidatService.secteurActivitePourCodeROME(query.codeROME).metiers.map(_.codeROME)
    val metiersSecteurSansMetierChoisi = metiersSecteur.filter(_ != query.codeROME)
    val selectCandidatsInteressesParMetierMemeSecteur = candidatTable.filter(c =>
      filtreMatching(c, query.typeRecruteur, query.codeDepartement) &&
        c.rechercheAutreMetier === true &&
        c.metiersRecherches @& metiers &&
        c.metiersEvalues @& metiersSecteurSansMetierChoisi
    ).sortBy(_.dateInscription).map(rechercheCandidatDtoShape)

    // Candidats qui sont intéréssés par ce métier et qui ont été évalués sur un métier d'un autre secteur
    val selectCandidatsInteressesParMetierAutreSecteur = candidatTable.filter(c =>
      filtreMatching(c, query.typeRecruteur, query.codeDepartement) &&
        c.numeroTelephone.isDefined &&
        c.indexerMatching &&
        c.rechercheAutreMetier === true &&
        c.metiersRecherches @& metiers &&
        !(c.metiersEvalues @& metiersSecteur)
    ).sortBy(_.dateInscription).map(rechercheCandidatDtoShape)

    for {
      candidatsEvaluesSurMetier <- database.run(selectCandidatsEvaluesSurMetier.result)
      candidatsInteressesParMetierMemeSecteur <- database.run(selectCandidatsInteressesParMetierMemeSecteur.result)
      candidatsInteressesParMetierAutreSecteur <- database.run(selectCandidatsInteressesParMetierAutreSecteur.result)
    } yield
      ResultatRechercheCandidatParMetier(
        candidatsEvaluesSurMetier = candidatsEvaluesSurMetier.toList.map(toRechercheCandidatDto),
        candidatsInteressesParMetierMemeSecteur = candidatsInteressesParMetierMemeSecteur.toList.map(toRechercheCandidatDto),
        candidatsInteressesParMetierAutreSecteur = candidatsInteressesParMetierAutreSecteur.toList.map(toRechercheCandidatDto)
      )
  }

  private def rechercheCandidatDtoShape(c: CandidatTable) =
    RechercheCandidatLifted(c.candidatId, c.nom, c.prenom, c.email, c.commune, c.metiersEvalues, c.metiersRecherches, c.rayonRecherche, c.numeroTelephone, c.cvId, c.cvTypeMedia)

  private def toRechercheCandidatDto(record: RechercheCandidatRecord): RechercheCandidatDto = {
    val metiersEvalues = record.metiersEvalues.map(referentielMetier.metierParCode)

    RechercheCandidatDto(
      candidatId = record.candidatId,
      nom = record.nom,
      prenom = record.prenom,
      email = record.email,
      commune = record.commune,
      metiersEvalues = metiersEvalues,
      habiletes = metiersEvalues.flatMap(m => referentielMetier.habiletesParMetier(m.codeROME)).distinct,
      metiersRecherchesParSecteur =
        record.metiersRecherches.flatMap(rechercheCandidatService.metierProposeParCode)
          .groupBy(m => rechercheCandidatService.secteurActivitePourCodeROME(m.codeROME)),
      rayonRecherche = record.rayonRecherche,
      numeroTelephone = record.numeroTelephone,
      cvId = record.cvId,
      cvTypeMedia = record.cvTypeMedia
    )
  }

  private def toCandidatPourConseillerDto(record: CandidatPourConseillerRecord): CandidatPourConseillerDto = {
    val metiersRecherches = record.metiersRecherches.flatMap(rechercheCandidatService.metierProposeParCode)
    val metiersEvalues = record.metiersEvalues.map(referentielMetier.metierParCode)

    CandidatPourConseillerDto(
      candidatId = record.candidatId,
      nom = record.nom,
      prenom = record.prenom,
      genre = record.genre,
      email = record.email,
      statutDemandeurEmploi = record.statutDemandeurEmploi,
      rechercheMetierEvalue = record.rechercheMetierEvalue,
      metiersEvalues = metiersEvalues,
      rechercheAutreMetier = record.rechercheAutreMetier,
      metiersRecherches = metiersRecherches,
      contacteParAgenceInterim = record.contacteParAgenceInterim,
      contacteParOrganismeFormation = record.contacteParOrganismeFormation,
      rayonRecherche = record.rayonRecherche,
      numeroTelephone = record.numeroTelephone,
      dateInscription = record.dateInscription
    )
  }

  private def filtreMatching(c: CandidatTable, typeRecruteur: TypeRecruteur, codeDepartement: Option[String]): Rep[Option[Boolean]] =
  // FIXME : faire en une condition sur un seul champ + modifier toutes les requetes de matching une fois que la projection peut traiter les evenements en séquentiel pour un même candidat (select avant mise à jour)
    c.numeroTelephone.isDefined &&
      c.metiersEvalues.length(dim = 1) > 0 &&
      c.indexerMatching &&
      filtreTypeRecruteur(c, typeRecruteur) &&
      filtreDepartement(c, codeDepartement)

  private def filtreTypeRecruteur(c: CandidatTable,
                                  typeRecruteur: TypeRecruteur): Rep[Option[Boolean]] = typeRecruteur match {
    case TypeRecruteur.AGENCE_INTERIM => c.contacteParAgenceInterim === true
    case TypeRecruteur.ORGANISME_FORMATION => c.contacteParOrganismeFormation === true
    case _ => Some(true)
  }

  private def filtreDepartement(c: CandidatTable,
                                 codeDepartement: Option[String]): Rep[Option[Boolean]] = codeDepartement match {
    case Some(code) => c.codePostal startsWith code
    case None => Some(true)
  }

  def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    database
      .run(candidatTable.map(c => (c.candidatId, c.nom, c.prenom, c.genre, c.email, c.dateInscription))
        += (event.candidatId, event.nom, event.prenom, event.genre, event.email, event.date))
      .map(_ => ())

  def onProfilModifieEvent(event: ProfilCandidatModifieEvent): Future[Unit] =
    database.run(modifierProfilQuery(event.candidatId).update((
      event.nom,
      event.prenom,
      event.email,
      event.genre
    ))).map(_ => ())

  def onCriteresRechercheModifiesEvent(event: CriteresRechercheModifiesEvent): Future[Unit] =
    database.run(modifierCriteresRechercheQuery(event.candidatId).update((
      Some(event.etreContacteParOrganismeFormation),
      Some(event.etreContacteParAgenceInterim),
      Some(event.rechercheMetierEvalue),
      Some(event.rechercheAutreMetier),
      Some(event.rayonRecherche),
      event.metiersRecherches.toList,
      !candidatsTesteurs.contains(event.candidatId) && (event.rechercheMetierEvalue || event.rechercheAutreMetier)
    ))).map(_ => ())

  def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit] =
    database.run(modifierNumeroTelephoneQuery(event.candidatId).update(
      Some(event.numeroTelephone)
    )).map(_ => ())

  def onStatutDemandeurEmploiModifieEvent(event: StatutDemandeurEmploiModifieEvent): Future[Unit] =
    database.run(modifierStatutDemandeurEmploiQuery(event.candidatId).update(
      Some(event.statutDemandeurEmploi)
    )).map(_ => ())

  def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] =
    database.run(modifierCVQuery(event.candidatId).update(
      Some(event.cvId),
      Some(event.typeMedia)
    )).map(_ => ())

  def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] =
    database.run(modifierCVQuery(event.candidatId).update(
      Some(event.cvId),
      Some(event.typeMedia)
    )).map(_ => ())

  def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit] =
    database.run(modifierAdresseQuery(event.candidatId).update((
      Some(event.adresse.codePostal),
      Some(event.adresse.libelleCommune)
    ))).map(_ => ())

  def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit] =
  //FIXME : est faite en une seule requete car la base va gérer le fait que deux evenements peuvent arriver très proches et que la projection n'attend pas la fin du traitement d'un event avant de passer à l'autre
    database.run(
      sqlu"""
            UPDATE candidats
            SET metiers_evalues = ${event.metier.value}::text || metiers_evalues
            WHERE candidat_id = ${event.candidatId.value}
          """).map(_ => ())

  def onRepriseEmploiDeclareeParConseillerEvent(event: RepriseEmploiDeclareeParConseillerEvent): Future[Unit] =
    database.run(repriseEmploiQuery(event.candidatId).update((
      Some(false),
      Some(false),
      Nil,
      false
    ))).map(_ => ())

}
