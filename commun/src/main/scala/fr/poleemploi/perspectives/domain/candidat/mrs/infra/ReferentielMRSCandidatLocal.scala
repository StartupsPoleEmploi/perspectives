package fr.poleemploi.perspectives.domain.candidat.mrs.infra

import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.mrs.{MRSValidee, ReferentielMRSCandidat}

import scala.concurrent.Future

/**
  * Referentiel qui intègre les MRS validées depuis un fichier CSV vers une table dans la base de l'application perspectives.
  */
class ReferentielMRSCandidatLocal(postgresqlMetierEvalueService: MRSValideePostgreSql) extends ReferentielMRSCandidat {

  override def metiersValidesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]] =
    postgresqlMetierEvalueService.metiersEvaluesParCandidat(peConnectId)
}
