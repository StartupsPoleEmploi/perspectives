package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRS

import scala.concurrent.Future

class ImportMRSLocalAdapter extends ImportMRS {

  override def integrerMRSValidees: Future[Unit] =
    Future.successful(())
}
