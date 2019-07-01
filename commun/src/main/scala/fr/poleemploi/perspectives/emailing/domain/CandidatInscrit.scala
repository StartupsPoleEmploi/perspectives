package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, Nom, Prenom}

case class CandidatInscrit(candidatId: CandidatId,
                           nom: Nom,
                           prenom: Prenom,
                           email: Email,
                           genre: Genre)
