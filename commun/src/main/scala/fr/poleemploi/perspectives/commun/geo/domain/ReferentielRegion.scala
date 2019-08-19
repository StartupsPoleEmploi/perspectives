package fr.poleemploi.perspectives.commun.geo.domain

import fr.poleemploi.perspectives.commun.domain.{Departement, Region}

import scala.concurrent.Future

trait ReferentielRegion {

  def regions: Future[List[Region]]

  def departements: Future[List[Departement]]
}