package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.ValueObject

case class Adresse(codePostal: String,
                   libelleCommune: String,
                   libellePays: String) extends ValueObject