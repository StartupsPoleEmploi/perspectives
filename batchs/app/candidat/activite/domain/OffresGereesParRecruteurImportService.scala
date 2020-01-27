package candidat.activite.domain

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.{CodeSafir, Coordonnees, Email}
import fr.poleemploi.perspectives.emailing.domain.{OffreAvecCoordonneesGereeParRecruteur, OffreGereeParRecruteur, OffreGereeParRecruteurAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, RechercheCandidatQueryResult, RechercheCandidatsQuery}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait OffresGereesParRecruteurImportService {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  def actorSystem: ActorSystem

  def baseUrl: String

  def correspondantsOffresParCodeSafir: Map[CodeSafir, Seq[Email]]

  def localisationService: LocalisationService

  def candidatQueryHandler: CandidatQueryHandler

  def mailjetWSAdapter: MailjetWSAdapter

  def importerOffres: Future[Stream[OffreGereeParRecruteur]]

  def envoyerCandidatsParMailPourOffreGereeParRecruteur(offresGereesParRecruteurAvecCandidats: Seq[OffreGereeParRecruteurAvecCandidats]): Future[Unit]

  def importerOffresGereesParRecruteur: Future[Stream[OffreGereeParRecruteurAvecCandidats]] =
    for {
      offres <- importerOffres.map(_.toList)
      coordonneesParCodePostal <- localisationService.localiserVilles(offres.map(_.lieuTravail))
      offresAvecCoordonnees = offres.flatMap(offre =>
        coordonneesParCodePostal
          .get(offre.lieuTravail)
          .map(coordonnees => buildOffreAvecCoordonneesGereeParRecruteur(offre, coordonnees))
      )
      offresAvecCandidats <- Future.sequence(offresAvecCoordonnees.map(offre =>
        rechercherCandidats(offre)
      )).map(_.filter(_.nbCandidats > 0))
      _ <- envoyerCandidatsParMailPourOffreGereeParRecruteur(offresAvecCandidats)
    } yield offresAvecCandidats.toStream

  private def buildOffreAvecCoordonneesGereeParRecruteur(offre: OffreGereeParRecruteur, coordonnees: Coordonnees) =
    OffreAvecCoordonneesGereeParRecruteur(
      offreId = offre.offreId,
      enseigne = offre.enseigne,
      nomCorrespondant = offre.nomCorrespondant,
      emailCorrespondant = offre.emailCorrespondant,
      codeSafir = offre.codeSafir,
      intitule = offre.intitule,
      codePostal = offre.codePostal,
      coordonnees = coordonnees,
      codeROME = offre.codeROME,
      lieuTravail = offre.lieuTravail,
      datePublication = offre.datePublication
    )

  private def rechercherCandidats(offre: OffreAvecCoordonneesGereeParRecruteur): Future[OffreGereeParRecruteurAvecCandidats] =
    candidatQueryHandler.handle(RechercheCandidatsQuery(
      typeRecruteur = TypeRecruteur.ENTREPRISE, // TODO voir si on peut deduire le type de recruteur a partir de l'extract ?
      codeSecteurActivite = None,
      codeROME = Some(offre.codeROME),
      coordonnees = Some(offre.coordonnees),
      nbPagesACharger = 0,
      page = None
    )).map(result => buildOffreGereeParRecruteurAvecCandidats(offre, result))

  private def buildOffreGereeParRecruteurAvecCandidats(offre: OffreAvecCoordonneesGereeParRecruteur, rechercheCandidatQueryResult: RechercheCandidatQueryResult) =
    OffreGereeParRecruteurAvecCandidats(
      offreId = offre.offreId,
      enseigne = offre.enseigne,
      nomCorrespondant = offre.nomCorrespondant,
      emailCorrespondant = offre.emailCorrespondant,
      codeSafir = offre.codeSafir,
      intitule = offre.intitule,
      codePostal = offre.codePostal,
      coordonnees = offre.coordonnees,
      codeROME = offre.codeROME,
      lieuTravail = offre.lieuTravail,
      datePublication = offre.datePublication,
      nbCandidats = rechercheCandidatQueryResult.nbCandidatsTotal
    )

}
