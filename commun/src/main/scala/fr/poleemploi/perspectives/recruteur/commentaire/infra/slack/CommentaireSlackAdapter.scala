package fr.poleemploi.perspectives.recruteur.commentaire.infra.slack

import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CommentaireSlackAdapter(config: CommentaireSlackConfig,
                              wsClient: WSClient) extends CommentaireService with WSAdapter {

  override def commenterListeCandidats(commentaire: CommentaireListeCandidats): Future[Unit] =
    wsClient
      .url(s"${config.webhookURL}")
      .addHttpHeaders(jsonContentType)
      .post(Json.obj(
        "text" ->
          s"""
             |Commentaire sur la recherche candidats de ${commentaire.nomRecruteur.capitalize} ${commentaire.prenomRecruteur.capitalize}, société ${commentaire.raisonSociale} :
             |Secteur : ${commentaire.labelSecteurActiviteRecherche.getOrElse("")}
             |Métier : ${commentaire.labelMetierRecherche.getOrElse("")}
             |Département : ${commentaire.labelLocalisationRecherche.getOrElse("")}
             |Commentaire : ${commentaire.commentaire}""".stripMargin
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())
}
