package fr.poleemploi.perspectives.infra.jackson

import fr.poleemploi.eventsourcing.infra.jackson.ValueObjectModule
import fr.poleemploi.perspectives.domain.candidat.StatutDemandeurEmploi
import fr.poleemploi.perspectives.domain.recruteur.{NumeroSiret, TypeRecruteur}
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

object PerspectivesValueObjectModule extends ValueObjectModule {

  addStringValueObject(classOf[NumeroTelephone], NumeroTelephone(_))
  addStringValueObject(classOf[NumeroSiret], NumeroSiret(_))
  addStringValueObject(classOf[Genre], Genre.from(_).get)
  addStringValueObject(classOf[Metier], Metier.from(_).get)
  addStringValueObject(classOf[TypeRecruteur], TypeRecruteur.from(_).get)
  addStringValueObject(classOf[StatutDemandeurEmploi], StatutDemandeurEmploi.from(_).get)
  addIntValueObject(classOf[RayonRecherche], RayonRecherche.from(_).get)
}
