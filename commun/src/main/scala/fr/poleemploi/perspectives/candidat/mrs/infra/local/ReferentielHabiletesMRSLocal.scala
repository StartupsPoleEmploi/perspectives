package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}

import scala.concurrent.Future

class ReferentielHabiletesMRSLocal extends ReferentielHabiletesMRS {

  override def habiletes(codeROME: CodeROME, codeDepartement: CodeDepartement): Future[List[Habilete]] =
    Future.successful(List(
      Habilete("Respecter des normes et des consignes"),
      Habilete("Travailler en équipe")
    ))

}
