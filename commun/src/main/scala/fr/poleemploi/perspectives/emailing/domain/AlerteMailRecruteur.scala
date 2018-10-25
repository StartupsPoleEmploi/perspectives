package fr.poleemploi.perspectives.emailing.domain

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{Departement, Email, Metier, SecteurActivite}
import fr.poleemploi.perspectives.recruteur.alerte.domain.FrequenceAlerte

sealed trait AlerteMailRecruteur {

  def prenom: String

  def email: Email

  def nbCandidats: Int

  def apresDateInscription: ZonedDateTime

  def lienConnexion: String

  def frequence: FrequenceAlerte
}

case class AlerteMailRecruteurMetier(prenom: String,
                                     email: Email,
                                     nbCandidats: Int,
                                     apresDateInscription: ZonedDateTime,
                                     metier: Metier,
                                     departement: Option[Departement],
                                     lienConnexion: String,
                                     frequence: FrequenceAlerte) extends AlerteMailRecruteur

case class AlerteMailRecruteurSecteur(prenom: String,
                                      email: Email,
                                      nbCandidats: Int,
                                      apresDateInscription: ZonedDateTime,
                                      secteurActivite: SecteurActivite,
                                      departement: Option[Departement],
                                      lienConnexion: String,
                                      frequence: FrequenceAlerte) extends AlerteMailRecruteur

case class AlerteMailRecruteurDepartement(prenom: String,
                                          email: Email,
                                          nbCandidats: Int,
                                          apresDateInscription: ZonedDateTime,
                                          departement: Departement,
                                          lienConnexion: String,
                                          frequence: FrequenceAlerte) extends AlerteMailRecruteur
