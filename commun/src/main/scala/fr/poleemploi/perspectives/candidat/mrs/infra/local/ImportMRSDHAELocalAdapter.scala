package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSDHAE

import scala.concurrent.Future

class ImportMRSDHAELocalAdapter extends ImportMRSDHAE {

  override def integrerMRSDHAEValidees: Future[Unit] =
    Future.successful(())
}
