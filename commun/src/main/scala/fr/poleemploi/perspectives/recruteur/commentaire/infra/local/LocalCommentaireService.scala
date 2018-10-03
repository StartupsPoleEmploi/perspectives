package fr.poleemploi.perspectives.recruteur.commentaire.infra.local

import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService}

import scala.concurrent.Future

class LocalCommentaireService extends CommentaireService {

  override def commenterListeCandidats(commentaire: CommentaireListeCandidats): Future[Unit] = {
    Future.successful(())
  }
}
