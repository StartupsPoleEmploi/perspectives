package fr.poleemploi.perspectives.candidat.mrs.infra.local

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, MRSValideeCandidat, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.commun.domain.CodeROME

import scala.concurrent.Future

class ReferentielMRSCandidatLocal extends ReferentielMRSCandidat {

  override def integrerMRSValidees: Future[Stream[MRSValideeCandidat]] = Future.successful(Stream.empty)

  override def mrsValideesParCandidat(candidatId: CandidatId): Future[List[MRSValidee]] =
    Future.successful(List(
      MRSValidee(
        codeROME = CodeROME("B1802"),
        dateEvaluation = LocalDate.now()
      ),
      MRSValidee(
        codeROME = CodeROME("I1307"),
        dateEvaluation = LocalDate.now()
      )
    ))

}
