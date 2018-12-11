package fr.poleemploi.perspectives.recruteur.commentaire.infra.slack

import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CommentaireSlackAdapter(config: SlackRecruteurConfig,
                              wsClient: WSClient) extends CommentaireService {

  override def commenterListeCandidats(commentaire: CommentaireListeCandidats): Future[Unit] =
    wsClient
      .url(s"${config.webhookURL}")
      .addHttpHeaders("Content-Type" -> "application/json")
      .post(Json.stringify(Json.obj(
        "text" ->
          s"""
             |Commentaire sur la recherche candidats de ${commentaire.nomRecruteur.capitalize} ${commentaire.prenomRecruteur.capitalize}, société ${commentaire.raisonSociale} :
             |Secteur : ${commentaire.labelSecteurActiviteRecherche.getOrElse("")}
             |Métier : ${commentaire.labelMetierRecherche.getOrElse("")}
             |Département : ${commentaire.labelLocalisationRecherche.getOrElse("")}
             |Commentaire : ${commentaire.commentaire}""".stripMargin
      )))
      .map(response => {
        if (response.status >= 400) {
          throw new RuntimeException(s"Erreur lors de l'appel à la notification slack. Code: ${response.status}. Reponse : ${response.body}")
        } else if (response.status != 200) {
          throw new RuntimeException(s"Statut non géré lors de l'appel à la notification slack. Code: ${response.status}. Reponse : ${response.body}")
        }
      })
}
