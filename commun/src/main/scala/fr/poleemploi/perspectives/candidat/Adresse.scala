package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.ValueObject

case class Adresse(voie: String,
                   codePostal: String,
                   libelleCommune: String,
                   libellePays: String) extends ValueObject
