package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import fr.poleemploi.perspectives.commun.domain.{CodeROME, Habilete}

import scala.concurrent.Future

class ReferentielHabiletesMRSLocalAdapter extends ReferentielHabiletesMRS {

  override def habiletes(codeROME: CodeROME): Future[Set[Habilete]] =
    Future.successful(Set(
      Habilete("Respecter des normes et des consignes"),
      Habilete("Travailler en équipe")
    ))

  override def codeROMEsAvecHabiletes: Future[List[CodeROME]] =
    Future.successful(
      List(
        CodeROME("A1402"),
        CodeROME("A1414"),
        CodeROME("B1802"),
        CodeROME("D1408"),
        CodeROME("D1507"),
        CodeROME("F1501"),
        CodeROME("F1603"),
        CodeROME("F1605"),
        CodeROME("G1401"),
        CodeROME("G1603"),
        CodeROME("G1803"),
        CodeROME("I1307"),
        CodeROME("I1604"),
        CodeROME("K1201"),
        CodeROME("K1304"),
        CodeROME("K1801"),
        CodeROME("K2104")
      )
    )
}
