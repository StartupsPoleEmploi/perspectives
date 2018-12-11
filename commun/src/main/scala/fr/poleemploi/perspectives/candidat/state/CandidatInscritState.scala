package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS

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

  override def connecter(context: CandidatContext,
                         command: ConnecterCandidatCommand,
                         localisationService: LocalisationService): Future[List[Event]] = {
    val candidatConnecteEvent = Future.successful(Some(CandidatConnecteEvent(command.id)))

    val profilCandidatModifieEvent = Future.successful(
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
      } else None)

    val adresseModifieeEvent = command.adresse.map(adresse =>
      if (!context.adresse.contains(adresse)) {
        localisationService.localiser(adresse).map(optCoordonnees => Some(
          AdresseModifieeEvent(
            candidatId = command.id,
            adresse = adresse,
            coordonnees = optCoordonnees
          )
        )).recover {
          case _: Throwable => Some(
            AdresseModifieeEvent(
              candidatId = command.id,
              adresse = adresse,
              coordonnees = None
            )
          )
        }
      } else Future.successful(None)
    ).getOrElse(Future.successful(None))

    val statutDemandeurEmploiModifieEvent = Future.successful(command.statutDemandeurEmploi.flatMap(statutDemandeurEmploi =>
      if (!context.statutDemandeurEmploi.contains(statutDemandeurEmploi)) {
        Some(StatutDemandeurEmploiModifieEvent(
          candidatId = command.id,
          statutDemandeurEmploi = statutDemandeurEmploi
        ))
      } else None
    ))

    Future.sequence(List(candidatConnecteEvent, profilCandidatModifieEvent, adresseModifieeEvent, statutDemandeurEmploiModifieEvent))
      .map(_.flatten)
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

  override def ajouterMRSValidee(context: CandidatContext, command: AjouterMRSValideesCommand,
                                 referentielHabiletesMRS: ReferentielHabiletesMRS): Future[List[Event]] = {
    // Un candidat peut potentiellement passer la même MRS à une date différente (la repasser) : aux projections de gérer si elles veulent afficher un historique ou simplement savoir les métiers validés
    val mrsDejaValidees = context.mrsValidees.intersect(command.mrsValidees)
    if (mrsDejaValidees.nonEmpty) {
      return Future.failed(new IllegalArgumentException(
        s"Le candidat ${command.id.value} a déjà validé les MRS suivantes : ${mrsDejaValidees.foldLeft("")((s, mrs) => s + '\n' + s"${mrs.codeROME} le ${mrs.dateEvaluation}")}"
      ))
    }

    Future.sequence(
    command.mrsValidees.map(m =>
      referentielHabiletesMRS.habiletes(
        codeROME = m.codeROME,
        codeDepartement = m.codeDepartement
      ).map(h =>
        MRSAjouteeEvent(
          candidatId = command.id,
          metier = m.codeROME,
          departement = m.codeDepartement,
          habiletes = h,
          dateEvaluation = m.dateEvaluation
        ))
      )
    )
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
