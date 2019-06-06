package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class DomaineFormation(value: String) extends StringValueObject

case class NiveauFormation(value: String) extends StringValueObject

case class Formation(anneeFin: Int,
                     intitule: String,
                     lieu: Option[String],
                     domaine: Option[DomaineFormation],
                     niveau: Option[NiveauFormation])