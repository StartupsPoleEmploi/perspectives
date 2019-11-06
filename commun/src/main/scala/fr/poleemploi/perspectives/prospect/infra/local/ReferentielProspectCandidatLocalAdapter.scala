package fr.poleemploi.perspectives.prospect.infra.local

import java.time.LocalDate

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.metier.domain.Metier
import fr.poleemploi.perspectives.prospect.domain.{ProspectCandidat, ReferentielProspectCandidat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielProspectCandidatLocalAdapter extends ReferentielProspectCandidat {

  private val prospectCandidat = ProspectCandidat(
    peConnectId = PEConnectId("1111-2222-3333-4444"),
    identifiantLocal = IdentifiantLocal("123456789A"),
    nom = Nom("Patulacci"),
    prenom = Prenom("Marcel"),
    email = Email("marcel.patulacci@pole-emploi.fr"),
    genre = Genre.HOMME,
    codeDepartement = CodeDepartement("35"),
    metier = Metier(
      codeROME = CodeROME("D1101"),
      label = "Boucherie Charcuterie"
    ),
    dateEvaluation = LocalDate.now
  )

  override def streamProspectsCandidats: Source[ProspectCandidat, NotUsed] =
    Source.fromIterator[ProspectCandidat](() => Iterator.fill(1)(prospectCandidat))

  override def ajouter(prospectsCandidats: Stream[ProspectCandidat]): Future[Unit] =
    Future.successful((): Unit)

  override def supprimer(email: Email): Future[Unit] =
    Future.successful((): Unit)

  override def find(email: Email): Future[Option[ProspectCandidat]] =
    Future.successful(Some(prospectCandidat.copy(email = email)))
}
