package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}

import scala.concurrent.Future

trait ReferentielHabiletesMRS {

  def habiletes(codeROME: CodeROME, codeDepartement: CodeDepartement): Future[List[Habilete]]

  /**
    * Liste les départements pour lesquels il y a des CodeROME avec des habiletés
    */
  def codeROMEsParDepartement: Future[Map[CodeDepartement, List[CodeROME]]]
}
