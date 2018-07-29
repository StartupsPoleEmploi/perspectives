package fr.poleemploi.perspectives.infra.jackson

import fr.poleemploi.eventsourcing.infra.jackson.ValueObjectModule
import fr.poleemploi.perspectives.domain.recruteur.{NumeroSiret, TypeRecruteur}
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone}

object PerspectivesValueObjectModule extends ValueObjectModule {

  addStringValueObject(classOf[NumeroTelephone], NumeroTelephone(_))
  addStringValueObject(classOf[NumeroSiret], NumeroSiret(_))
  addStringValueObject(classOf[Genre], Genre.from(_).get)
  addStringValueObject(classOf[Metier], Metier.from(_).get)
  addStringValueObject(classOf[TypeRecruteur], TypeRecruteur.from(_).get)
}
