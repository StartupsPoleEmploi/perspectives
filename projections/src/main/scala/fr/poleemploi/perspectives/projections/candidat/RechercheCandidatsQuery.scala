package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, _}
import fr.poleemploi.perspectives.metier.domain.Metier
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

case class CandidatRechercheRecruteurDto(candidatId: CandidatId,
                                         nom: Nom,
                                         prenom: Prenom,
                                         email: Email,
                                         metiersValides: List[Metier],
                                         habiletes: Set[Habilete],
                                         metiersValidesRecherches: List[Metier],
                                         metiersRecherches: List[Metier],
                                         numeroTelephone: NumeroTelephone,
                                         rayonRecherche: Option[RayonRecherche],
                                         commune: String,
                                         cvId: Option[CVId],
                                         cvTypeMedia: Option[TypeMedia]) {

  def nomCV: Option[String] = cvTypeMedia.map(t => s"${prenom.value} ${nom.value}.${TypeMedia.getExtensionFichier(t)}")
}

object CandidatRechercheRecruteurDto {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[CandidatRechercheRecruteurDto] = (c: CandidatRechercheRecruteurDto) =>
    Json.writes[CandidatRechercheRecruteurDto].writes(c) ++ Json.obj("nomCV" -> c.nomCV)
}

case class KeysetRechercherCandidats(score: Option[Int],
                                     dateInscription: Long,
                                     candidatId: Option[CandidatId])

object KeysetRechercherCandidats {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[KeysetRechercherCandidats] = Json.writes[KeysetRechercherCandidats]
}

case class RechercheCandidatQueryResult(candidats: List[CandidatRechercheRecruteurDto],
                                        nbCandidats: Int,
                                        nbCandidatsTotal: Int,
                                        pages: List[KeysetRechercherCandidats],
                                        pageSuivante: Option[KeysetRechercherCandidats]) extends QueryResult

object RechercheCandidatQueryResult {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[RechercheCandidatQueryResult] = Json.writes[RechercheCandidatQueryResult]
}