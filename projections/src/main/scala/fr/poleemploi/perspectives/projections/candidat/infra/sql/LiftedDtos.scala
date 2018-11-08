package fr.poleemploi.perspectives.projections.candidat.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import slick.lifted.Rep

case class CandidatContactRecruteurLifted(contacteParAgenceInterim: Rep[Option[Boolean]],
                                          contacteParOrganismeFormation: Rep[Option[Boolean]])

case class CandidatPourConseillerLifted(candidatId: Rep[CandidatId],
                                        nom: Rep[String],
                                        prenom: Rep[String],
                                        genre: Rep[Genre],
                                        email: Rep[Email],
                                        statutDemandeurEmploi: Rep[Option[StatutDemandeurEmploi]],
                                        rechercheMetierEvalue: Rep[Option[Boolean]],
                                        metiersEvalues: Rep[List[CodeROME]],
                                        rechercheAutreMetier: Rep[Option[Boolean]],
                                        metiersRecherches: Rep[List[CodeROME]],
                                        contacteParAgenceInterim: Rep[Option[Boolean]],
                                        contacteParOrganismeFormation: Rep[Option[Boolean]],
                                        rayonRecherche: Rep[Option[RayonRecherche]],
                                        numeroTelephone: Rep[Option[NumeroTelephone]],
                                        dateInscription: Rep[ZonedDateTime],
                                        dateDerniereConnexion: Rep[ZonedDateTime])

case class CandidatSaisieCriteresRechercheLifted(candidatId: Rep[CandidatId],
                                                 nom: Rep[String],
                                                 prenom: Rep[String],
                                                 rechercheMetierEvalue: Rep[Option[Boolean]],
                                                 metiersEvalues: Rep[List[CodeROME]],
                                                 rechercheAutreMetier: Rep[Option[Boolean]],
                                                 metiersRecherches: Rep[List[CodeROME]],
                                                 contacteParAgenceInterim: Rep[Option[Boolean]],
                                                 contacteParOrganismeFormation: Rep[Option[Boolean]],
                                                 rayonRecherche: Rep[Option[RayonRecherche]],
                                                 numeroTelephone: Rep[Option[NumeroTelephone]],
                                                 cvId: Rep[Option[CVId]],
                                                 cvTypeMedia: Rep[Option[TypeMedia]])

case class CandidatRechercheLifted(candidatId: Rep[CandidatId],
                                   nom: Rep[String],
                                   prenom: Rep[String],
                                   email: Rep[Email],
                                   commune: Rep[Option[String]],
                                   metiersEvalues: Rep[List[CodeROME]],
                                   habiletes: Rep[List[Habilete]],
                                   metiersRecherches: Rep[List[CodeROME]],
                                   rayonRecherche: Rep[Option[RayonRecherche]],
                                   numeroTelephone: Rep[Option[NumeroTelephone]],
                                   cvId: Rep[Option[CVId]],
                                   cvTypeMedia: Rep[Option[TypeMedia]])