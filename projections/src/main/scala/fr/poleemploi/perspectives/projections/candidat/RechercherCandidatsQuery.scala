package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.TypeRecruteur

sealed trait RechercherCandidatsQuery extends Query[RechercheCandidatQueryResult]

case class RechercherCandidatsParDepartementQuery(typeRecruteur: TypeRecruteur,
                                                  codeDepartement: CodeDepartement,
                                                  apresDateInscription: Option[ZonedDateTime] = None) extends RechercherCandidatsQuery

case class RechercherCandidatsParSecteurQuery(typeRecruteur: TypeRecruteur,
                                              codeSecteurActivite: CodeSecteurActivite,
                                              codeDepartement: Option[CodeDepartement] = None,
                                              apresDateInscription: Option[ZonedDateTime] = None) extends RechercherCandidatsQuery

case class RechercherCandidatsParMetierQuery(typeRecruteur: TypeRecruteur,
                                             codeROME: CodeROME,
                                             codeDepartement: Option[CodeDepartement] = None,
                                             apresDateInscription: Option[ZonedDateTime] = None) extends RechercherCandidatsQuery

sealed trait RechercheCandidatQueryResult extends QueryResult {

  def nbCandidats: Int
}

case class RechercheCandidatParDepartementQueryResult(candidats: List[CandidatRechercheDto]) extends RechercheCandidatQueryResult {

  override val nbCandidats: Int = candidats.size
}

case class RechercheCandidatParSecteurQueryResult(candidatsEvaluesSurSecteur: List[CandidatRechercheDto],
                                                  candidatsInteressesParAutreSecteur: List[CandidatRechercheDto]) extends RechercheCandidatQueryResult {

  override val nbCandidats: Int = candidatsEvaluesSurSecteur.size + candidatsInteressesParAutreSecteur.size
}

case class RechercheCandidatParMetierQueryResult(candidatsEvaluesSurMetier: List[CandidatRechercheDto],
                                                 candidatsInteressesParMetierMemeSecteur: List[CandidatRechercheDto],
                                                 candidatsInteressesParMetierAutreSecteur: List[CandidatRechercheDto]) extends RechercheCandidatQueryResult {

  override val nbCandidats: Int = candidatsEvaluesSurMetier.size + candidatsInteressesParMetierMemeSecteur.size + candidatsInteressesParMetierAutreSecteur.size
}

case class CandidatRechercheDto(candidatId: CandidatId,
                                nom: String,
                                prenom: String,
                                email: Email,
                                commune: Option[String],
                                metiersEvalues: List[Metier],
                                habiletes: List[Habilete],
                                metiersRecherchesParSecteur: Map[SecteurActivite, List[Metier]],
                                rayonRecherche: Option[RayonRecherche],
                                numeroTelephone: Option[NumeroTelephone],
                                cvId: Option[CVId],
                                cvTypeMedia: Option[TypeMedia]) {

  def possedeCV: Boolean = cvId.isDefined

  def nomCV: Option[String] = cvTypeMedia.map(t => s"$nom-$prenom.${TypeMedia.getExtensionFichier(t)}")
}