package candidat.activite.infra.mailjet

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import candidat.activite.domain.ImportOffresGereesParRecruteurService
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.Coordonnees
import fr.poleemploi.perspectives.emailing.domain.{OffreAvecCoordonneesGereeParRecruteur, OffreGereeParRecruteur, OffreGereeParRecruteurAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportOffresGereesParRecruteurCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, RechercheCandidatQueryResult, RechercheCandidatsQuery}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetImportOffresGereesParRecruteur(actorSystem: ActorSystem,
                                            baseUrl: String,
                                            importOffresGereesParRecruteurCSVAdapter: ImportOffresGereesParRecruteurCSVAdapter,
                                            localisationService: LocalisationService,
                                            candidatQueryHandler: CandidatQueryHandler,
                                            mailjetWSAdapter: MailjetWSAdapter) extends ImportOffresGereesParRecruteurService with Logging {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override def importerOffresGereesParRecruteur: Future[Stream[OffreGereeParRecruteurAvecCandidats]] =
    for {
      offres <- importOffresGereesParRecruteurCSVAdapter.importerOffres.map(_.toList)
      coordonneesParCodePostal <- localisationService.localiserCodesPostaux(offres.map(_.codePostal))
      offresAvecCoordonnees = offres.flatMap(offre =>
        coordonneesParCodePostal
          .get(offre.codePostal)
          .map(coordonnees => buildOffreAvecCoordonneesGereeParRecruteur(offre, coordonnees))
      )
      offresAvecCandidats <- Future.sequence(offresAvecCoordonnees.map(offre =>
        rechercherCandidats(offre)
      )).map(_.filter(_.nbCandidats > 0))
      _ = logger.debug(s"Nombre d'offres gérées directement par des recruteurs avec des candidats disponibles dans Perspectives : ${offresAvecCandidats.size}")
      _ <- mailjetWSAdapter.envoyerCandidatsPourOffreGereeParRecruteur(baseUrl, offresAvecCandidats)
    } yield offresAvecCandidats.toStream

  private def buildOffreAvecCoordonneesGereeParRecruteur(offre: OffreGereeParRecruteur, coordonnees: Coordonnees) =
    OffreAvecCoordonneesGereeParRecruteur(
      offreId = offre.offreId,
      enseigne = offre.enseigne,
      nomCorrespondant = offre.nomCorrespondant,
      emailCorrespondant = offre.emailCorrespondant,
      intitule = offre.intitule,
      codePostal = offre.codePostal,
      coordonnees = coordonnees,
      codeROME = offre.codeROME,
      lieuTravail = offre.lieuTravail
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
      intitule = offre.intitule,
      codePostal = offre.codePostal,
      coordonnees = offre.coordonnees,
      codeROME = offre.codeROME,
      lieuTravail = offre.lieuTravail,
      nbCandidats = rechercheCandidatQueryResult.nbCandidatsTotal
    )
}
