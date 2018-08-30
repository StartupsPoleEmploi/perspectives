package fr.poleemploi.perspectives.projections.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}

case class RecruteurDto(recruteurId: RecruteurId,
                        nom: String,
                        prenom: String,
                        email: Email,
                        genre: Genre,
                        typeRecruteur: Option[TypeRecruteur],
                        raisonSociale: Option[String],
                        numeroSiret: Option[NumeroSiret],
                        numeroTelephone: Option[NumeroTelephone],
                        contactParCandidats: Option[Boolean],
                        dateInscription: ZonedDateTime) {

  val profilComplet: Boolean =
    List(typeRecruteur, raisonSociale, numeroSiret, numeroTelephone, contactParCandidats).forall(_.isDefined)
}
