package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CandidatInscritState extends CandidatState {

  override def name: String = "Inscrit"

  // FIXME : vérification de l'existence des codes ROME dans le référentiel
  override def modifierCriteres(context: CandidatContext, command: ModifierCriteresRechercheCommand): List[Event] = {
    val criteresRechercheModifiesEvent =
      if (!context.rechercheMetierEvalue.contains(command.rechercheMetierEvalue) ||
        !context.rechercheAutreMetier.contains(command.rechercheAutreMetier) ||
        !context.metiersRecherches.forall(command.metiersRecherches.contains) ||
        !command.metiersRecherches.forall(context.metiersRecherches.contains) ||
        !context.etreContacteParAgenceInterim.contains(command.etreContacteParAgenceInterim) ||
        !context.etreContacteParOrganismeFormation.contains(command.etreContacteParOrganismeFormation) ||
        !context.rayonRecherche.contains(command.rayonRecherche)) {
        Some(CriteresRechercheModifiesEvent(
          candidatId = command.id,
          rechercheMetierEvalue = command.rechercheMetierEvalue,
          rechercheAutreMetier = command.rechercheAutreMetier,
          metiersRecherches = command.metiersRecherches,
          etreContacteParAgenceInterim = command.etreContacteParAgenceInterim,
          etreContacteParOrganismeFormation = command.etreContacteParOrganismeFormation,
          rayonRecherche = command.rayonRecherche
        ))
      } else None

    val numeroTelephoneModifieEvent =
      if (!context.numeroTelephone.contains(command.numeroTelephone)) {
        Some(NumeroTelephoneModifieEvent(
          candidatId = command.id,
          numeroTelephone = command.numeroTelephone
        ))
      } else None

    List(criteresRechercheModifiesEvent, numeroTelephoneModifieEvent).flatten
  }

  override def connecter(context: CandidatContext, command: ConnecterCandidatCommand): List[Event] = {
    val candidatConnecteEvent = Some(CandidatConnecteEvent(command.id))

    val profilCandidatModifieEvent =
      if (!context.nom.contains(command.nom) ||
        !context.prenom.contains(command.prenom) ||
        !context.email.contains(command.email) ||
        !context.genre.contains(command.genre)) {
        Some(ProfilCandidatModifieEvent(
          candidatId = command.id,
          nom = command.nom,
          prenom = command.prenom,
          email = command.email,
          genre = command.genre
        ))
      } else None

    val adresseModifieeEvent = command.adresse.flatMap(adresse =>
      if (command.adresse.isDefined && !context.adresse.contains(command.adresse.get)) {
        Some(AdresseModifieeEvent(
          candidatId = command.id,
          adresse = command.adresse.get
        ))
      } else None
    )

    val statutDemandeurEmploiModifieEvent = command.statutDemandeurEmploi.flatMap(statutDemandeurEmploi =>
      if (!context.statutDemandeurEmploi.contains(statutDemandeurEmploi)) {
        Some(StatutDemandeurEmploiModifieEvent(
          candidatId = command.id,
          statutDemandeurEmploi = statutDemandeurEmploi
        ))
      } else None
    )

    List(candidatConnecteEvent, profilCandidatModifieEvent, adresseModifieeEvent, statutDemandeurEmploiModifieEvent).flatten
  }

  override def ajouterCV(context: CandidatContext, command: AjouterCVCommand, cvService: CVService): Future[List[Event]] = {
    if (context.cvId.isDefined) {
      return Future.failed(new IllegalArgumentException(s"Impossible d'ajouter un CV au candidat ${command.id.value}, il existe déjà"))
    }

    val cvId = cvService.nextIdentity
    cvService.save(
      cvId = cvId,
      candidatId = command.id,
      nomFichier = command.nomFichier,
      typeMedia = command.typeMedia,
      path = command.path
    ).map(_ => List(
      CVAjouteEvent(
        candidatId = command.id,
        cvId = cvId,
        typeMedia = command.typeMedia
      )
    ))
  }

  override def remplacerCV(context: CandidatContext, command: RemplacerCVCommand, cvService: CVService): Future[List[Event]] = {
    if (context.cvId.isEmpty) {
      return Future.failed(new IllegalArgumentException(s"Impossible de remplacer le CV inexistant du candidat ${command.id.value}"))
    }

    cvService.update(
      cvId = command.cvId,
      nomFichier = command.nomFichier,
      typeMedia = command.typeMedia,
      path = command.path
    ).map(_ => List(
      CVRemplaceEvent(
        candidatId = command.id,
        cvId = command.cvId,
        typeMedia = command.typeMedia
      )
    ))
  }

  override def ajouterMRSValidee(context: CandidatContext, command: AjouterMRSValideesCommand): List[Event] = {
    val mrsDejaValidees = context.mrsValidees.intersect(command.mrsValidees)
    if (mrsDejaValidees.nonEmpty) {
      throw new IllegalArgumentException(
        s"Le candidat ${command.id.value} a déjà validé les MRS suivantes : ${mrsDejaValidees.foldLeft("")((s, mrs) => s + '\n' + s"${mrs.codeROME} le ${mrs.dateEvaluation}")}"
      )
    }

    command.mrsValidees.map(m => MRSAjouteeEvent(
      candidatId = command.id,
      metier = m.codeROME,
      dateEvaluation = m.dateEvaluation
    ))
  }

  override def declarerRepriseEmploiParConseiller(context: CandidatContext, command: DeclarerRepriseEmploiParConseillerCommand): List[Event] = {
    if (context.rechercheEmploi.contains(false)) {
      throw new IllegalArgumentException(s"Le candidat ${command.id.value} n'est pas en recherche d'emploi")
    }

    List(RepriseEmploiDeclareeParConseillerEvent(
      candidatId = command.id,
      conseillerId = command.conseillerId
    ))
  }
}
