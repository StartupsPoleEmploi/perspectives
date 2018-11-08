package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}

import scala.concurrent.Future

case class HabiletesMRS(codeROME: CodeROME,
                        codeDepartement: CodeDepartement,
                        habiletes: List[Habilete])

trait ImportHabiletesMRS {

  def integrerHabiletesMRS: Future[Stream[HabiletesMRS]]
}
