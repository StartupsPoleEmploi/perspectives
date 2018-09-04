package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}

case class ProfilRecruteurDto(recruteurId: RecruteurId,
                              typeRecruteur: Option[TypeRecruteur],
                              raisonSociale: Option[String],
                              numeroSiret: Option[NumeroSiret],
                              numeroTelephone: Option[NumeroTelephone],
                              contactParCandidats: Option[Boolean]) {
  val profilComplet: Boolean =
    List(typeRecruteur, raisonSociale, numeroSiret, numeroTelephone, contactParCandidats).forall(_.isDefined)
}
