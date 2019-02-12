package fr.poleemploi.perspectives.offre.domain

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.StringValueObject
import fr.poleemploi.perspectives.commun.domain.{Email, Metier, NumeroTelephone}

case class OffreId(value: String) extends StringValueObject

case class Offre(id: OffreId,
                 urlOrigine: String,
                 intitule: String,
                 description: Option[String],
                 libelleLieuTravail: String,
                 typeContrat: String,
                 libelleTypeContrat: String,
                 libelleSalaire: Option[String],
                 libelleDureeTravail: Option[String],
                 libelleExperience: String,
                 metier: Metier,
                 competences: List[String],
                 nomEntreprise: Option[String],
                 descriptionEntreprise: Option[String],
                 effectifEntreprise: Option[String],
                 dateActualisation: ZonedDateTime,
                 nomContact: Option[String],
                 telephoneContact: Option[NumeroTelephone],
                 emailContact: Option[Email],
                 urlPostuler: Option[String],
                 coordonneesContact1: Option[String],
                 coordonneesContact2: Option[String],
                 coordonneesContact3: Option[String]) {

  override def equals(that: Any): Boolean =
    that match {
      case that: Offre => that.isInstanceOf[Offre] && this.id == that.id
      case _ => false
    }

  override def hashCode: Int =
    31 + (if (id == null) 0 else id.hashCode)
}