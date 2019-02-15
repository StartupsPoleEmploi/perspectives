package fr.poleemploi.perspectives.offre.domain

import fr.poleemploi.perspectives.commun.domain.{CodeSecteurActivite, RayonRecherche}

case class CriteresRechercheOffre(motCle: Option[String],
                                  codePostal: Option[String],
                                  rayonRecherche: Option[RayonRecherche],
                                  typesContrats: List[TypeContrat],
                                  secteursActivites: List[CodeSecteurActivite]) {

  val experience: Experience = Experience.DEBUTANT
}
