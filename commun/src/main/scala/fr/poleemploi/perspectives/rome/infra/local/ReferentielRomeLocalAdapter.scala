package fr.poleemploi.perspectives.rome.infra.local

import fr.poleemploi.perspectives.commun.domain.{CodeAppellation, CodeROME}
import fr.poleemploi.perspectives.rome.domain.{Appellation, ReferentielRome}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielRomeLocalAdapter extends ReferentielRome {

  override def rechargerAppellations: Future[Unit] =
    Future.successful((): Unit)

  def appellationsRecherche(query: String): Future[Seq[Appellation]] =
    Future.successful(Seq(
      Appellation(
        codeAppellation = CodeAppellation("11711"),
        codeROME = CodeROME("L1401"),
        label = "Catcheur / Catcheuse"
      ),
      Appellation(
        codeAppellation = CodeAppellation("11713"),
        codeROME = CodeROME("L1401"),
        label = "Cavalier / Cavalière d'entraînement"
      ),
      Appellation(
        codeAppellation = CodeAppellation("11714"),
        codeROME = CodeROME("L1401"),
        label = "Cavalier dresseur / Cavalière dresseuse de chevaux"
      ),
      Appellation(
        codeAppellation = CodeAppellation("11716"),
        codeROME = CodeROME("L1401"),
        label = "Cavalier professionnel / Cavalière professionnelle"
      )
    ))

}
