package fr.poleemploi.perspectives.candidat.activite.infra.csv

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

/**
  * Represente la reprise d'activite des candidat extraits au format CSV par la DSI Pole Emploi
  */
case class ActiviteCandidatCsv(peConnectId: PEConnectId,
                               nom: Nom,
                               prenom: Prenom,
                               nbHeuresTravaillees: Int,
                               dateActualisation: LocalDate)
