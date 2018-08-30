package fr.poleemploi.perspectives.candidat

import java.util.UUID

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.conseiller.ConseillerId

import scala.collection.mutable.ListBuffer

class CandidatBuilder {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  private var events: ListBuffer[Event] = ListBuffer()

  def avecInscription(nom: Option[String] = None,
                      prenom: Option[String] = None,
                      email: Option[String] = None,
                      genre: Option[Genre] = None): CandidatBuilder = {
    events += CandidatInscritEvent(
      candidatId = candidatId,
      nom = nom.getOrElse("planteur"),
      prenom = prenom.getOrElse("jacques"),
      email = email.getOrElse("jacques.planteur@mail.com"),
      genre = genre.getOrElse(Genre.HOMME)
    )
    this
  }

  def avecCV(cvId: CVId): CandidatBuilder = {
    events += CVAjouteEvent(
      candidatId = candidatId,
      cvId = cvId
    )
    this
  }

  def avecMRSValidee(mrsValidee: MRSValidee): CandidatBuilder = {
    events += MRSAjouteeEvent(
      candidatId = candidatId,
      metier = mrsValidee.codeROME,
      dateEvaluation = mrsValidee.dateEvaluation
    )
    this
  }

  def avecAdresse(adresse: Adresse): CandidatBuilder = {
    events += AdresseModifieeEvent(
      candidatId = candidatId,
      adresse = adresse
    )
    this
  }

  def avecStatutDemandeurEmploi(statutDemandeurEmploi: StatutDemandeurEmploi): CandidatBuilder = {
    events += StatutDemandeurEmploiModifieEvent(
      candidatId = candidatId,
      statutDemandeurEmploi = statutDemandeurEmploi
    )
    this
  }

  def avecCriteresRecherche(rechercheMetierEvalue: Option[Boolean] = None,
                            rechercheAutreMetier: Option[Boolean] = None,
                            metiersRecherches: Option[Set[CodeROME]] = None,
                            etreContacteParAgenceInterim: Option[Boolean] = None,
                            etreContacteParOrganismeFormation: Option[Boolean] = None,
                            rayonRecherche: Option[RayonRecherche] = None): CandidatBuilder = {
    events += CriteresRechercheModifiesEvent(
      candidatId = candidatId,
      rechercheMetierEvalue = rechercheMetierEvalue.getOrElse(true),
      rechercheAutreMetier = rechercheAutreMetier.getOrElse(true),
      metiersRecherches = metiersRecherches.getOrElse(Set.empty),
      etreContacteParAgenceInterim = etreContacteParAgenceInterim.getOrElse(true),
      etreContacteParOrganismeFormation = etreContacteParOrganismeFormation.getOrElse(true),
      rayonRecherche = rayonRecherche.getOrElse(RayonRecherche.MAX_10)
    )
    this
  }

  def avecNumeroTelephone(numeroTelephone: Option[NumeroTelephone] = None): CandidatBuilder = {
    events += NumeroTelephoneModifieEvent(
      candidatId = candidatId,
      numeroTelephone = numeroTelephone.getOrElse(NumeroTelephone("0123456789"))
    )
    this
  }

  def avecRepriseEmploiDeclaree(conseillerId: ConseillerId): CandidatBuilder = {
    events += RepriseEmploiDeclareeParConseillerEvent(
      candidatId = candidatId,
      conseillerId =conseillerId
    )
    this
  }

  def build: Candidat = {
    val candidat = new Candidat(
      id = candidatId,
      version = events.size,
      events = events.toList
    )
    events = ListBuffer()
    candidat
  }

}
