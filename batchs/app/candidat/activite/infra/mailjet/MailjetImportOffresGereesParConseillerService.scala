package candidat.activite.infra.mailjet

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import candidat.activite.domain.ImportOffresGereesParConseillerService
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.Coordonnees
import fr.poleemploi.perspectives.emailing.domain.{OffreAvecCoordonneesGereeParConseiller, OffreGereeParConseiller, OffreGereeParConseillerAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportOffresGereesParConseillerCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, RechercheCandidatQueryResult, RechercheCandidatsQuery}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetImportOffresGereesParConseillerService(actorSystem: ActorSystem,
                                            baseUrl: String,
                                            importOffresGereesParConseillerCSVAdapter: ImportOffresGereesParConseillerCSVAdapter,
                                            localisationService: LocalisationService,
                                            candidatQueryHandler: CandidatQueryHandler,
                                            mailjetWSAdapter: MailjetWSAdapter) extends ImportOffresGereesParConseillerService with Logging {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override def importerOffresGereesParConseiller: Future[Stream[OffreGereeParConseillerAvecCandidats]] =
    for {
      offres <- importOffresGereesParConseillerCSVAdapter.importerOffres.map(_.toList)
      coordonneesParCodePostal <- localisationService.localiserCodesPostaux(offres.map(_.codePostal))
      offresAvecCoordonnees = offres.flatMap(offre =>
        coordonneesParCodePostal
          .get(offre.codePostal)
          .map(coordonnees => buildOffreAvecCoordonneesGereeParConseiller(offre, coordonnees))
      )
      offresAvecCandidats <- Future.sequence(offresAvecCoordonnees.map(offre => {
        rechercherCandidats(offre)
      }
      )).map(_.filter(_.nbCandidats > 0))
      _ = logger.debug(s"Nombre d'offres gérées par les conseillés avec des candidats disponibles dans Perspectives : ${offresAvecCandidats.size}")
      _ <- mailjetWSAdapter.envoyerCandidatsPourOffreGereeParConseiller(baseUrl, offresAvecCandidats)
    } yield offresAvecCandidats.toStream

  private def buildOffreAvecCoordonneesGereeParConseiller(offre: OffreGereeParConseiller, coordonnees: Coordonnees) =
    OffreAvecCoordonneesGereeParConseiller(
      offreId = offre.offreId,
      enseigne = offre.enseigne,
      emailCorrespondant = offre.emailCorrespondant,
      intitule = offre.intitule,
      codePostal = offre.codePostal,
      coordonnees = coordonnees,
      codeROME = offre.codeROME,
      lieuTravail = offre.lieuTravail
    )

  private def rechercherCandidats(offre: OffreAvecCoordonneesGereeParConseiller): Future[OffreGereeParConseillerAvecCandidats] =
    candidatQueryHandler.handle(RechercheCandidatsQuery(
      typeRecruteur = TypeRecruteur.ENTREPRISE, // TODO voir quel type de recruteur mettre pour un conseiller
      codeSecteurActivite = None,
      codeROME = Some(offre.codeROME),
      coordonnees = Some(offre.coordonnees),
      nbPagesACharger = 1,
      page = None
    )).map(result => {buildOffreGereeParConseillerAvecCandidats(offre, result)})

  private def buildOffreGereeParConseillerAvecCandidats(offre: OffreAvecCoordonneesGereeParConseiller, rechercheCandidatQueryResult: RechercheCandidatQueryResult) =
    OffreGereeParConseillerAvecCandidats(
      offreId = offre.offreId,
      enseigne = offre.enseigne,
      emailCorrespondant = offre.emailCorrespondant,
      intitule = offre.intitule,
      codePostal = offre.codePostal,
      coordonnees = offre.coordonnees,
      codeROME = offre.codeROME,
      lieuTravail = offre.lieuTravail,
      nbCandidats = rechercheCandidatQueryResult.nbCandidatsTotal
    )
}
