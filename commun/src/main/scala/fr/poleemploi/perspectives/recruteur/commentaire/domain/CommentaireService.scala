package fr.poleemploi.perspectives.recruteur.commentaire.domain

import scala.concurrent.Future

trait CommentaireService {

  def commenterListeCandidats(commentaire: CommentaireListeCandidats): Future[Unit]
}
