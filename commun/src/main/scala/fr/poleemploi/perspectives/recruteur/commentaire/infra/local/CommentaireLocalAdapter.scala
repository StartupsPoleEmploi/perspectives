package fr.poleemploi.perspectives.recruteur.commentaire.infra.local

import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService}

import scala.concurrent.Future

class CommentaireLocalAdapter extends CommentaireService {

  override def commenterListeCandidats(commentaire: CommentaireListeCandidats): Future[Unit] = {
    Future.successful(println(
      s"""
         |Commentaire sur la recherche candidats de ${commentaire.nomRecruteur.capitalize} ${commentaire.prenomRecruteur.capitalize}, société ${commentaire.raisonSociale} :
         |Secteur : ${commentaire.labelSecteurActiviteRecherche.getOrElse("")}
         |Métier : ${commentaire.labelMetierRecherche.getOrElse("")}
         |Département : ${commentaire.labelLocalisationRecherche.getOrElse("")}
         |Commentaire : ${commentaire.commentaire}""".stripMargin
    ))
  }
}
