package fr.poleemploi.perspectives.candidat.activite.domain

import fr.poleemploi.perspectives.authentification.infra.autologin.JwtToken
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Email, Nom, Prenom}

case class EmailingDisponibiliteCandidatAvecEmail(candidatId: CandidatId,
                                                  nom: Nom,
                                                  prenom: Prenom,
                                                  email: Email,
                                                  autologinToken: JwtToken)
