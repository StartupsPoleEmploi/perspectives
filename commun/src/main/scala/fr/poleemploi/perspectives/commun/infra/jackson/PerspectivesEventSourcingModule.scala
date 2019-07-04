package fr.poleemploi.perspectives.commun.infra.jackson

import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingModule
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.conseiller.ConseillerId
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, StatutRecruteur, TypeRecruteur}

object PerspectivesEventSourcingModule extends EventSourcingModule {

  addAggregateId(classOf[CandidatId], CandidatId)
  addAggregateId(classOf[RecruteurId], RecruteurId)
  addAggregateId(classOf[ConseillerId], ConseillerId)

  addStringValueObject(classOf[StatutCandidat], StatutCandidat(_))
  addStringValueObject(classOf[StatutRecruteur], StatutRecruteur(_))
  addStringValueObject(classOf[NumeroTelephone], NumeroTelephone(_))
  addStringValueObject(classOf[NumeroSiret], NumeroSiret(_))
  addStringValueObject(classOf[Genre], Genre(_))
  addStringValueObject(classOf[Nom], Nom(_))
  addStringValueObject(classOf[Prenom], Prenom(_))
  addStringValueObject(classOf[Email], Email(_))
  addStringValueObject(classOf[CodeROME], CodeROME)
  addStringValueObject(classOf[CodeSecteurActivite], CodeSecteurActivite)
  addStringValueObject(classOf[CodeDomaineProfessionnel], CodeDomaineProfessionnel)
  addStringValueObject(classOf[CodeDepartement], CodeDepartement)
  addStringValueObject(classOf[TypeRecruteur], TypeRecruteur(_))
  addStringValueObject(classOf[StatutDemandeurEmploi], StatutDemandeurEmploi(_))
  addStringValueObject(classOf[CVId], CVId)
  addStringValueObject(classOf[TypeMedia], TypeMedia(_))
  addStringValueObject(classOf[Habilete], Habilete)
  addStringValueObject(classOf[UniteLongueur], UniteLongueur(_))
  addStringValueObject(classOf[CentreInteret], CentreInteret(_))
  addStringValueObject(classOf[NiveauLangue], NiveauLangue(_))
  addStringValueObject(classOf[TempsTravail], TempsTravail(_))
  addStringValueObject(classOf[SavoirEtre], SavoirEtre(_))
  addStringValueObject(classOf[NiveauSavoirFaire], NiveauSavoirFaire(_))
  addStringValueObject(classOf[NiveauFormation], NiveauFormation)
  addStringValueObject(classOf[DomaineFormation], DomaineFormation)
}