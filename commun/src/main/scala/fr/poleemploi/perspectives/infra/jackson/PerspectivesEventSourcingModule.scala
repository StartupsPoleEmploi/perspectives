package fr.poleemploi.perspectives.infra.jackson

import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingModule
import fr.poleemploi.perspectives.domain.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.domain.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

object PerspectivesEventSourcingModule extends EventSourcingModule {

  addAggregateId(classOf[CandidatId], CandidatId)
  addAggregateId(classOf[RecruteurId], RecruteurId)

  addStringValueObject(classOf[NumeroTelephone], NumeroTelephone(_))
  addStringValueObject(classOf[NumeroSiret], NumeroSiret(_))
  addStringValueObject(classOf[Genre], Genre.from(_).get)
  addStringValueObject(classOf[Metier], Metier.from(_).get)
  addStringValueObject(classOf[TypeRecruteur], TypeRecruteur.from(_).get)
  addStringValueObject(classOf[StatutDemandeurEmploi], StatutDemandeurEmploi.from(_).get)

  addIntValueObject(classOf[RayonRecherche], RayonRecherche.from(_).get)
}
