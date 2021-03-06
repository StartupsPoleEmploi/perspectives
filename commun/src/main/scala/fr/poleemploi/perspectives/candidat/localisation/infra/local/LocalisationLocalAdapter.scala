package fr.poleemploi.perspectives.candidat.localisation.infra.local

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.Coordonnees

import scala.concurrent.Future

class LocalisationLocalAdapter extends LocalisationService {

  override def localiser(adresse: Adresse): Future[Option[Coordonnees]] =
    Future.successful(Some(Coordonnees(
      latitude = 46.621373,
      longitude = -1.847949
    )))

  override def localiserVilles(villes: Seq[String]): Future[Map[String, Coordonnees]] =
    Future.successful(villes.map(ville =>
      ville -> Coordonnees(
        latitude = 46.621373,
        longitude = -1.847949
      )
    ).toMap)
}
