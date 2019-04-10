package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

// FIXME : en fait il faut faire un Code à part : le label sera dans HabileteDTO
case class Habilete(value: String) extends StringValueObject