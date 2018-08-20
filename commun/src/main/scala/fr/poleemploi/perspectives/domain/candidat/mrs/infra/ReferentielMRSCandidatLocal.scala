package fr.poleemploi.perspectives.domain.candidat.mrs.infra

import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.mrs.{MRSValidee, ReferentielMRSCandidat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Referentiel qui intègre les MRS validées depuis un fichier CSV vers une table dans la base de l'application perspectives.
  */
class ReferentielMRSCandidatLocal(mrsValideeCSVLoader: MRSValideeCSVLoader,
                                  mrsValideesPostgresSql: MRSValideePostgreSql) extends ReferentielMRSCandidat {

  override def integrerMRSValidees: Future[Unit] =
    mrsValideeCSVLoader
      .load
      .flatMap(mrsValideesPostgresSql.ajouter)
      .map(_ => ())

  override def metiersValidesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]] =
    mrsValideesPostgresSql.metiersEvaluesParCandidat(peConnectId)
}
