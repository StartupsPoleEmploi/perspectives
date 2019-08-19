package fr.poleemploi.perspectives.projections.recruteur.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}
import slick.lifted.Rep

case class RecruteurPourConseillerLifted(recruteurId: Rep[RecruteurId],
                                         nom: Rep[Nom],
                                         prenom: Rep[Prenom],
                                         typeRecruteur: Rep[Option[TypeRecruteur]],
                                         raisonSociale: Rep[Option[String]],
                                         commune: Rep[Option[String]],
                                         codePostal: Rep[Option[String]],
                                         contactParCandidats: Rep[Option[Boolean]],
                                         dateInscription: Rep[ZonedDateTime],
                                         dateDerniereConnexion: Rep[ZonedDateTime])