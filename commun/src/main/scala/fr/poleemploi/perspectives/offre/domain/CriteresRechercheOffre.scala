package fr.poleemploi.perspectives.offre.domain

import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, RayonRecherche}

case class CriteresRechercheOffre(motCle: Option[String],
                                  codePostal: Option[String],
                                  rayonRecherche: Option[RayonRecherche],
                                  typesContrats: List[TypeContrat],
                                  secteursActivites: List[CodeSecteurActivite],
                                  metiers: List[CodeROME]) {

  val experience: Experience = Experience.DEBUTANT
}
