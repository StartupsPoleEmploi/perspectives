package fr.poleemploi.perspectives.prospect.domain

import java.time.LocalDate

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.commun.domain.Email

import scala.concurrent.Future

trait ReferentielProspectCandidat {

  def streamProspectsCandidats(dateMaxEvaluationMrs: Option[LocalDate]): Source[ProspectCandidat, NotUsed]

  def ajouter(prospectsCandidats: Stream[ProspectCandidat]): Future[Unit]

  def supprimer(email: Email): Future[Unit]

  def find(email: Email): Future[Option[ProspectCandidat]]

}
