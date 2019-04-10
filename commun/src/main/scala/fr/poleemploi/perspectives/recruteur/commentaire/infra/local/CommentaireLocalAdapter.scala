package fr.poleemploi.perspectives.recruteur.commentaire.infra.local

import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService}

import scala.concurrent.Future

class CommentaireLocalAdapter extends CommentaireService {

  override def commenterListeCandidats(commentaire: CommentaireListeCandidats): Future[Unit] = {
    Future.successful(println(
      s"""
         |Commentaire sur la recherche candidats de ${commentaire.nomRecruteur.value} ${commentaire.prenomRecruteur.value}, société ${commentaire.raisonSociale} :
         ${commentaire.secteurActivite.map(s => s"|Secteur : $s").getOrElse("")}
         ${commentaire.metier.map(m => s"|Métier : $m").getOrElse("")}
         ${commentaire.localisation.map(l => s"|Localisation : $l").getOrElse("")}
         |Commentaire : ${commentaire.commentaire}""".stripMargin
    ))
  }
}
