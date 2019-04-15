package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}

import scala.concurrent.Future

class ReferentielHabiletesMRSLocalAdapter extends ReferentielHabiletesMRS {

  override def habiletes(codeROME: CodeROME, codeDepartement: CodeDepartement): Future[Set[Habilete]] =
    Future.successful(Set(
      Habilete("Respecter des normes et des consignes"),
      Habilete("Travailler en équipe")
    ))

  override def codeROMEsParDepartement: Future[Map[CodeDepartement, List[CodeROME]]] =
    Future.successful(
      Map(
        CodeDepartement("49") -> List(
          CodeROME("A1402"),
          CodeROME("A1414"),
          CodeROME("B1802"),
          CodeROME("D1408"),
          CodeROME("D1507"),
        ),
        CodeDepartement("53") -> List(
          CodeROME("F1501"),
          CodeROME("F1603"),
          CodeROME("F1605")
        ),
        CodeDepartement("72") -> List(
          CodeROME("G1401"),
          CodeROME("G1603"),
          CodeROME("G1803")
        ),
        CodeDepartement("85") -> List(
          CodeROME("I1307"),
          CodeROME("I1604"),
          CodeROME("K1201"),
          CodeROME("K1304"),
          CodeROME("K1801"),
          CodeROME("K2104")
        )
      )
    )
}
