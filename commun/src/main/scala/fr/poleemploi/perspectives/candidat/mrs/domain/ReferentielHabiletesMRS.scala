package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Habilete}

import scala.concurrent.Future

trait ReferentielHabiletesMRS {

  def habiletes(codeROME: CodeROME): Future[Set[Habilete]]

  /**
    * Liste les départements pour lesquels il y a habiletés
    */
  def codeROMEsAvecHabiletes: Future[List[CodeROME]]
}
