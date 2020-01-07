package fr.poleemploi.perspectives.candidat.localisation.domain

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.commun.domain.Coordonnees

import scala.concurrent.Future

trait LocalisationService {

  def localiser(adresse: Adresse): Future[Option[Coordonnees]]

  def localiserVilles(villes: Seq[String]): Future[Map[String, Coordonnees]]
}
