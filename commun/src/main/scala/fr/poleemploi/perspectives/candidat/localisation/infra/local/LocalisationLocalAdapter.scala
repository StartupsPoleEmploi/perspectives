package fr.poleemploi.perspectives.candidat.localisation.infra.local

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.{CodePostal, Coordonnees}

import scala.concurrent.Future

class LocalisationLocalAdapter extends LocalisationService {

  override def localiser(adresse: Adresse): Future[Option[Coordonnees]] =
    Future.successful(Some(Coordonnees(
      latitude = 46.621373,
      longitude = -1.847949
    )))

  override def localiserCodesPostaux(codesPostaux: Seq[CodePostal]): Future[Map[CodePostal, Coordonnees]] =
    Future.successful(codesPostaux.map(codePostal =>
      codePostal -> Coordonnees(
        latitude = 46.621373,
        longitude = -1.847949
      )
    ).toMap)
}
