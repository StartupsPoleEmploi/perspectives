package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, _}
import fr.poleemploi.perspectives.projections.metier.MetierDTO
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.json.{Json, Writes}

case class RechercheCandidatsQuery(typeRecruteur: TypeRecruteur,
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

case class RechercheCandidatParLocalisationQueryResult(candidats: List[CandidatRechercheRecruteurDto],
                                                       nbCandidats: Int,
                                                       nbCandidatsTotal: Int,
                                                       pages: List[KeysetRechercherCandidats],
                                                       pageSuivante: Option[KeysetRechercherCandidats]) extends RechercheCandidatQueryResult

case class RechercheCandidatParSecteurQueryResult(candidatsEvaluesSurSecteur: List[CandidatRechercheRecruteurDto],
                                                  candidatsInteressesParAutreSecteur: List[CandidatRechercheRecruteurDto],
                                                  nbCandidats: Int,
                                                  nbCandidatsTotal: Int,
                                                  pages: List[KeysetRechercherCandidats],
                                                  pageSuivante: Option[KeysetRechercherCandidats]) extends RechercheCandidatQueryResult

case class RechercheCandidatParMetierQueryResult(candidatsEvaluesSurMetier: List[CandidatRechercheRecruteurDto],
                                                 candidatsInteressesParMetier: List[CandidatRechercheRecruteurDto],
                                                 nbCandidats: Int,
                                                 nbCandidatsTotal: Int,
                                                 pages: List[KeysetRechercherCandidats],
                                                 pageSuivante: Option[KeysetRechercherCandidats]) extends RechercheCandidatQueryResult

case class CandidatRechercheRecruteurDto(candidatId: CandidatId,
                                         nom: Nom,
                                         prenom: Prenom,
                                         email: Email,
                                         metiersValides: List[MetierDTO],
                                         habiletes: Set[Habilete],
                                         metiersRecherches: List[MetierDTO],
                                         numeroTelephone: NumeroTelephone,
                                         rayonRecherche: Option[RayonRecherche],
                                         commune: String,
                                         cvId: Option[CVId],
                                         cvTypeMedia: Option[TypeMedia]) {

  def possedeCV: Boolean = cvId.isDefined

  def nomCV: Option[String] = cvTypeMedia.map(t => s"${prenom.value} ${nom.value}.${TypeMedia.getExtensionFichier(t)}")
}

case class KeysetRechercherCandidats(score: Option[Int],
                                     dateInscription: Long,
                                     candidatId: Option[CandidatId])

object KeysetRechercherCandidats {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats.formatCandidatId

  implicit val writes: Writes[KeysetRechercherCandidats] = Json.writes[KeysetRechercherCandidats]
}