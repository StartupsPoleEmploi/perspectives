package fr.poleemploi.perspectives.projections.recruteur.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import slick.lifted.Rep

case class ProfilRecruteurLifted(recruteurId: Rep[RecruteurId],
                                 typeRecruteur: Rep[Option[TypeRecruteur]],
                                 raisonSociale: Rep[Option[String]],
                                 numeroSiret: Rep[Option[NumeroSiret]],
                                 numeroTelephone: Rep[Option[NumeroTelephone]],
                                 contactParCandidats: Rep[Option[Boolean]])

case class RecruteurPourConseillerLifted(recruteurId: Rep[RecruteurId],
                                         nom: Rep[Nom],
                                         prenom: Rep[Prenom],
                                         email: Rep[Email],
                                         genre: Rep[Genre],
                                         typeRecruteur: Rep[Option[TypeRecruteur]],
                                         raisonSociale: Rep[Option[String]],
                                         numeroSiret: Rep[Option[NumeroSiret]],
                                         numeroTelephone: Rep[Option[NumeroTelephone]],
                                         contactParCandidats: Rep[Option[Boolean]],
                                         dateInscription: Rep[ZonedDateTime],
                                         dateDerniereConnexion: Rep[ZonedDateTime])