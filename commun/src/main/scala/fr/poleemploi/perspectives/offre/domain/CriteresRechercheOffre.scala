package fr.poleemploi.perspectives.offre.domain

import fr.poleemploi.perspectives.commun.domain.{CodeDomaineProfessionnel, CodeROME, CodeSecteurActivite}

case class CriteresRechercheOffre(motsCles: List[String],
                                  codePostal: Option[String],
                                  rayonRecherche: Option[RayonRecherche],
                                  typesContrats: List[TypeContrat],
                                  secteursActivites: List[CodeSecteurActivite],
                                  codesROME: List[CodeROME],
                                  codesDomaineProfessionnels: List[CodeDomaineProfessionnel],
                                  page: Option[PageOffres]) {

  val experience: Experience = Experience.DEBUTANT
}
