package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSPEConnectAdapter(importMRSValideePEConnect: ImportMRSValideePEConnect,
                                importMRSDHAEValideePEConnect: ImportMRSDHAEValideePEConnect)
  extends ImportMRS {

  override def integrerMRSValidees: Future[Unit] =
    importMRSValideePEConnect.integrerFichiers.map(_ => ())

  override def integrerMRSDHAEValidees: Future[Unit] =
    importMRSDHAEValideePEConnect.integrerFichiers.map(_ => ())
}
