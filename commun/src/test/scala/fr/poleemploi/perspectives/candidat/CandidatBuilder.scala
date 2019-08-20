package fr.poleemploi.perspectives.candidat

import java.time.LocalDate
import java.util.UUID

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, _}
import fr.poleemploi.perspectives.conseiller.ConseillerId

import scala.collection.mutable.ListBuffer

class CandidatBuilder {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  private var events: ListBuffer[Event] = ListBuffer()

  def avecInscription(nom: Option[Nom] = None,
                      prenom: Option[Prenom] = None,
                      email: Option[Email] = None,
                      genre: Option[Genre] = None): CandidatBuilder = {
    events += CandidatInscritEvent(
      candidatId = candidatId,
      nom = nom.getOrElse(Nom("planteur")),
      prenom = prenom.getOrElse(Prenom("jacques")),
      email = email.getOrElse(Email("jacques.planteur@mail.com")),
      genre = genre.getOrElse(Genre.HOMME)
    )
    this
  }

  def avecCV(cvId: CVId, typeMedia: Option[TypeMedia] = None): CandidatBuilder = {
    events += CVAjouteEvent(
      candidatId = candidatId,
      cvId = cvId,
      typeMedia = typeMedia.getOrElse(TypeMedia.PDF)
    )
    this
  }

  def avecMRSValidee(mrsValidee: MRSValidee,
                     habiletes: Set[Habilete] = Set.empty): CandidatBuilder = {
    events += MRSAjouteeEvent(
      candidatId = candidatId,
      codeROME = mrsValidee.codeROME,
      departement = mrsValidee.codeDepartement,
      dateEvaluation = mrsValidee.dateEvaluation,
      habiletes =
        if (habiletes.isEmpty)
          Set(
            Habilete("S'adapter au changement"),
            Habilete("Maintenir son attention dans la durée")
          )
        else habiletes,
      isDHAE = mrsValidee.isDHAE
    )
    this
  }

  def avecAdresse(adresse: Adresse,
                  coordonnees: Option[Coordonnees] = None): CandidatBuilder = {
    events += AdresseModifieeEvent(
      candidatId = candidatId,
      adresse = adresse,
      coordonnees = coordonnees.getOrElse(Coordonnees(48.864716, 2.349014))
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

  def avecCentresInteret(centresInteret: List[CentreInteret]): CandidatBuilder = {
    events += CentresInteretModifiesEvent(
      candidatId = candidatId,
      centresInteret = centresInteret
    )
    this
  }

  def avecLangues(langues: List[Langue]): CandidatBuilder = {
    events += LanguesModifieesEvent(
      candidatId = candidatId,
      langues = langues
    )
    this
  }

  def avecPermis(permis: List[Permis]): CandidatBuilder = {
    events += PermisModifiesEvent(
      candidatId = candidatId,
      permis = permis
    )
    this
  }

  def avecSavoirEtre(savoirEtre: List[SavoirEtre]): CandidatBuilder = {
    events += SavoirEtreModifiesEvent(
      candidatId = candidatId,
      savoirEtre = savoirEtre
    )
    this
  }

  def avecSavoirFaire(savoirFaire: List[SavoirFaire]): CandidatBuilder = {
    events += SavoirFaireModifiesEvent(
      candidatId = candidatId,
      savoirFaire = savoirFaire
    )
    this
  }

  def avecFormations(formations: List[Formation]): CandidatBuilder = {
    events += FormationsModifieesEvent(
      candidatId = candidatId,
      formations = formations
    )
    this
  }

  def avecExperiencesProfessionnelles(experiencesProfessionnelles: List[ExperienceProfessionnelle]): CandidatBuilder = {
    events += ExperiencesProfessionnellesModifieesEvent(
      candidatId = candidatId,
      experiencesProfessionnelles = experiencesProfessionnelles
    )
    this
  }

  def avecVisibiliteRecruteur(contactRecruteur: Option[Boolean] = None,
                              contactFormation: Option[Boolean] = None): CandidatBuilder = {
    events += VisibiliteRecruteurModifieeEvent(
      candidatId = candidatId,
      contactRecruteur = contactRecruteur.getOrElse(true),
      contactFormation = contactFormation.getOrElse(true)
    )
    this
  }

  def avecCriteresRecherche(codesROMEValidesRecherches: Set[CodeROME] = Set.empty,
                            codesROMERecherches: Set[CodeROME] = Set.empty,
                            codesDomaineProfessionnelRecherches: Set[CodeDomaineProfessionnel] = Set.empty,
                            localisationRecherche: Option[LocalisationRecherche] = None,
                            tempsTravailRecherche: Option[TempsTravail] = None): CandidatBuilder = {
    events += CriteresRechercheModifiesEvent(
      candidatId = candidatId,
      localisationRecherche = localisationRecherche.getOrElse(LocalisationRecherche(
        commune = "La Roche Sur Yon",
        codePostal = "85300",
        coordonnees = Coordonnees(1.42, 48.5),
        rayonRecherche = Some(RayonRecherche.MAX_10)
      )),
      codesROMEValidesRecherches = codesROMEValidesRecherches,
      codesROMERecherches = codesROMERecherches,
      codesDomaineProfessionnelRecherches = codesDomaineProfessionnelRecherches,
      tempsTravailRecherche = tempsTravailRecherche.getOrElse(TempsTravail.TEMPS_PLEIN)
    )
    this
  }

  def avecDisponibilites(candidatEnRecherche: Option[Boolean] = None,
                         emploiTrouveGracePerspectives: Option[Boolean] = None,
                         prochaineDisponibilite: Option[LocalDate] = None): CandidatBuilder = {
    events += DisponibilitesModifieesEvent(
      candidatId = candidatId,
      candidatEnRecherche = candidatEnRecherche.getOrElse(true),
      emploiTrouveGracePerspectives = emploiTrouveGracePerspectives.getOrElse(true),
      prochaineDisponibilite = prochaineDisponibilite
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
      conseillerId = conseillerId
    )
    this
  }

  def build: Candidat = {
    val candidat = Candidat(
      id = candidatId,
      version = events.size,
      state = CandidatContext().apply(events.toList)
    )
    events = ListBuffer()
    candidat
  }

}
