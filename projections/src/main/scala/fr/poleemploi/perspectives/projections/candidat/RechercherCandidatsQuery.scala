package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, _}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.{JsPath, Writes}

case class RechercherCandidatsQuery(typeRecruteur: TypeRecruteur,
                                    codeSecteurActivite: Option[CodeSecteurActivite],
                                    codeROME: Option[CodeROME],
                                    coordonnees: Option[Coordonnees],
                                    nbPagesACharger: Int,
                                    page: Option[KeysetRechercherCandidats]) extends Query[RechercheCandidatQueryResult] {
  val nbCandidatsParPage: Int = 25
}

sealed trait RechercheCandidatQueryResult extends QueryResult {

  def nbCandidats: Int

  def nbCandidatsTotal: Int

  def pages: List[KeysetRechercherCandidats]

  def pageSuivante: Option[KeysetRechercherCandidats]
}

case class RechercheCandidatParLocalisationQueryResult(candidats: List[CandidatRechercheDto],
                                                       nbCandidats: Int,
                                                       nbCandidatsTotal: Int,
                                                       pages: List[KeysetRechercherCandidats],
                                                       pageSuivante: Option[KeysetRechercherCandidats]) extends RechercheCandidatQueryResult

case class RechercheCandidatParSecteurQueryResult(candidatsEvaluesSurSecteur: List[CandidatRechercheDto],
                                                  candidatsInteressesParAutreSecteur: List[CandidatRechercheDto],
                                                  nbCandidats: Int,
                                                  nbCandidatsTotal: Int,
                                                  pages: List[KeysetRechercherCandidats],
                                                  pageSuivante: Option[KeysetRechercherCandidats]) extends RechercheCandidatQueryResult

case class RechercheCandidatParMetierQueryResult(candidatsEvaluesSurMetier: List[CandidatRechercheDto],
                                                 candidatsInteressesParMetier: List[CandidatRechercheDto],
                                                 nbCandidats: Int,
                                                 nbCandidatsTotal: Int,
                                                 pages: List[KeysetRechercherCandidats],
                                                 pageSuivante: Option[KeysetRechercherCandidats]) extends RechercheCandidatQueryResult

case class CandidatRechercheDto(candidatId: CandidatId,
                                nom: String,
                                prenom: String,
                                email: Email,
                                metiersEvalues: List[Metier],
                                habiletes: List[Habilete],
                                metiersRecherches: List[Metier],
                                numeroTelephone: NumeroTelephone,
                                rayonRecherche: RayonRecherche,
                                commune: String,
                                cvId: Option[CVId],
                                cvTypeMedia: Option[TypeMedia]) {

  def possedeCV: Boolean = cvId.isDefined

  def nomCV: Option[String] = cvTypeMedia.map(t => s"$nom-$prenom.${TypeMedia.getExtensionFichier(t)}")
}

case class KeysetRechercherCandidats(score: Option[Int],
                                     dateInscription: Long,
                                     candidatId: Option[CandidatId])

object KeysetRechercherCandidats {

  implicit val writes: Writes[KeysetRechercherCandidats] = (
    (JsPath \ "score").writeNullable[Int] and
      (JsPath \ "dateInscription").write[Long] and
      (JsPath \ "candidatId").writeNullable[CandidatId]
    ) (unlift(KeysetRechercherCandidats.unapply))
}