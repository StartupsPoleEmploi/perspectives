package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielHabiletesMRS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CandidatInscritState extends CandidatState {

  override def connecter(context: CandidatContext,
                         command: ConnecterCandidatCommand): List[Event] = {
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

    List(candidatConnecteEvent, profilCandidatModifieEvent).flatten
  }

  override def modifierProfil(context: CandidatContext, command: ModifierProfilCandidatCommand, localisationService: LocalisationService): Future[List[Event]] = {
    val adresseModifieeEvent = command.adresse.map(adresse =>
      if (!context.adresse.contains(adresse)) {
        localisationService.localiser(adresse).map(optCoordonnees =>
          for {
            coordonnees <- optCoordonnees if !context.coordonnees.contains(coordonnees)
          } yield {
            AdresseModifieeEvent(
              candidatId = command.id,
              adresse = adresse,
              coordonnees = coordonnees
            )
          }
        ).recover {
          case _: Throwable => None
        }
      } else Future.successful(None)
    ).getOrElse(Future.successful(None))

    val statutDemandeurEmploiModifieEvent =
      Future(command.statutDemandeurEmploi.flatMap(statutDemandeurEmploi =>
        if (!context.statutDemandeurEmploi.contains(statutDemandeurEmploi))
          Some(StatutDemandeurEmploiModifieEvent(
            candidatId = command.id,
            statutDemandeurEmploi = statutDemandeurEmploi
          ))
        else None
      ))

    val centreInteretsModifiesEvent =
      Future(
        if (context.centresInteret.sortBy(_.value) != command.centresInteret.sortBy(_.value))
          Some(CentresInteretModifiesEvent(
            candidatId = command.id,
            centresInteret = command.centresInteret
          ))
        else None
      )

    val languesModifieesEvent =
      Future(
        if (context.langues.sortBy(_.label) != command.langues.sortBy(_.label))
          Some(LanguesModifieesEvent(
            candidatId = command.id,
            langues = command.langues
          ))
        else None
      )

    val permisModifiesEvent = Future(
      if (context.permis.sortBy(_.code) != command.permis.sortBy(_.code))
        Some(PermisModifiesEvent(
          candidatId = command.id,
          permis = command.permis
        ))
      else None
    )

    val savoirEtreModifiesEvent = Future(
      if (context.savoirEtre.sortBy(_.value) != command.savoirEtre.sortBy(_.value))
        Some(SavoirEtreModifiesEvent(
          candidatId = command.id,
          savoirEtre = command.savoirEtre
        ))
      else None
    )

    val savoirFaireModifiesEvent = Future(
      if (context.savoirFaire.sortBy(_.label) != command.savoirFaire.sortBy(_.label))
        Some(SavoirFaireModifiesEvent(
          candidatId = command.id,
          savoirFaire = command.savoirFaire
        ))
      else None
    )

    val formationsModifieesEvent = Future(
      if (context.formations.sortWith((f1, f2) => f1.anneeFin > f2.anneeFin && f1.intitule < f2.intitule)
        != command.formations.sortWith((f1, f2) => f1.anneeFin > f2.anneeFin && f1.intitule < f2.intitule))
        Some(FormationsModifieesEvent(
          candidatId = command.id,
          formations = command.formations
        ))
      else None
    )

    val experiencesModifieesEvent = Future(
      if (context.experiencesProfessionnelles.sortWith((e1, e2) => e1.dateDebut.isBefore(e2.dateDebut) && e1.intitule < e2.intitule)
        != command.experiencesProfessionnelles.sortWith((e1, e2) => e1.dateDebut.isBefore(e2.dateDebut) && e1.intitule < e2.intitule))
        Some(ExperiencesProfessionnellesModifieesEvent(
          candidatId = command.id,
          experiencesProfessionnelles = command.experiencesProfessionnelles
        ))
      else None
    )

    Future.sequence(List(adresseModifieeEvent, statutDemandeurEmploiModifieEvent, centreInteretsModifiesEvent, languesModifieesEvent, permisModifiesEvent, savoirEtreModifiesEvent, savoirFaireModifiesEvent, formationsModifieesEvent, experiencesModifieesEvent))
      .map(_.flatten)
  }

  override def modifierCriteresRecherche(context: CandidatContext, command: ModifierCriteresRechercheCommand): List[Event] = {
    require(!command.contactFormation || (command.contactFormation && command.numeroTelephone.nonEmpty), "Le numéro de téléphone doit être renseigné lorsque le contactFormation est souhaité")
    require(!command.contactRecruteur || (command.contactRecruteur && command.numeroTelephone.nonEmpty), "Le numéro de téléphone doit être renseigné lorsque le contactRecruteur est souhaité")
    val codesROMEValides = context.mrsValidees.map(_.codeROME)
    require(command.codesROMEValidesRecherches.forall(c => codesROMEValides.contains(c)), "Un codeROME ne fait pas partie des codesROME validés par le candidat")

    val visibiliteRecruteurModifieeEvent =
      if (!context.contactRecruteur.contains(command.contactRecruteur) ||
        !context.contactFormation.contains(command.contactFormation))
        Some(VisibiliteRecruteurModifieeEvent(
          candidatId = command.id,
          contactFormation = command.contactFormation,
          contactRecruteur = command.contactRecruteur,
        ))
      else None

    val numeroTelephoneModifieEvent = command.numeroTelephone.flatMap(n =>
      if (!context.numeroTelephone.contains(n)) {
        Some(NumeroTelephoneModifieEvent(
          candidatId = command.id,
          numeroTelephone = command.numeroTelephone.get
        ))
      } else None
    )

    val criteresRechercheModifiesEvent =
      if (!context.codesROMEValidesRecherches.forall(command.codesROMEValidesRecherches.contains) ||
        !context.codesROMERecherches.forall(command.codesROMERecherches.contains) ||
        !context.codesDomaineProfessionnelRecherches.forall(command.codesDomaineProfessionnelRecherches.contains) ||
        !context.localisationRecherche.contains(command.localisationRecherche) ||
        !context.tempsTravailRecherche.contains(command.tempsTravailRecherche)) {
        Some(CriteresRechercheModifiesEvent(
          candidatId = command.id,
          localisationRecherche = command.localisationRecherche,
          codesROMEValidesRecherches = command.codesROMEValidesRecherches,
          codesROMERecherches = command.codesROMERecherches,
          codesDomaineProfessionnelRecherches = command.codesDomaineProfessionnelRecherches,
          tempsTravailRecherche = Some(command.tempsTravailRecherche)
        ))
      } else None

    List(visibiliteRecruteurModifieeEvent, numeroTelephoneModifieEvent, criteresRechercheModifiesEvent).flatten
  }

  override def ajouterCV(context: CandidatContext, command: AjouterCVCommand, cvService: CVService): Future[List[Event]] = {
    require(context.cvId.isEmpty, s"Impossible d'ajouter un CV au candidat ${command.id.value}, il existe déjà")
    require(context.nom.isDefined, "Impossible d'ajouter un CV à un candidat sans nom")
    require(context.prenom.isDefined, "Impossible d'ajouter un CV à un candidat sans prénom")

    val cvId = cvService.nextIdentity
    cvService.save(
      cvId = cvId,
      candidatId = command.id,
      nomFichier = buildNomCV(context).get,
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
    require(context.cvId.isDefined, s"Impossible de remplacer le CV inexistant du candidat ${command.id.value}")
    require(context.nom.isDefined, "Impossible d'ajouter un CV à un candidat sans nom")
    require(context.prenom.isDefined, "Impossible d'ajouter un CV à un candidat sans prénom")

    cvService.update(
      cvId = command.cvId,
      nomFichier = buildNomCV(context).get,
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

  private def buildNomCV(context: CandidatContext): Option[String] =
    for {
      nom <- context.nom
      prenom <- context.prenom
    } yield s"${prenom.value} ${nom.value}"

  override def ajouterMRSValidee(context: CandidatContext,
                                 command: AjouterMRSValideesCommand,
                                 referentielHabiletesMRS: ReferentielHabiletesMRS): Future[List[Event]] = {
    val mrsSansDoublons = command.mrsValidees.foldLeft(List[MRSValidee]())((acc, mrsValidee) =>
      if (acc.exists(m => m.codeROME == mrsValidee.codeROME && m.codeDepartement == mrsValidee.codeDepartement))
        acc
      else
        mrsValidee :: acc
    )
    if (mrsSansDoublons.size != command.mrsValidees.size) {
      return Future.failed(new IllegalArgumentException(s"Impossible d'ajouter des MRS au candidat ${command.id.value} : la commande contient des MRS avec le même métier pour le même département"))
    }
    val mrsDejaValidees = context.mrsValidees.filter(m => command.mrsValidees.exists(c => c.codeROME == m.codeROME && c.codeDepartement == m.codeDepartement))
    if (mrsDejaValidees.nonEmpty) {
      return Future.failed(new IllegalArgumentException(
        s"Le candidat ${command.id.value} a déjà validé les métiers suivants : ${mrsDejaValidees.foldLeft("")((s, mrs) => s + '\n' + s"${mrs.codeROME.value} dans le département ${mrs.codeDepartement.value}")}"
      ))
    }

    Future.sequence(
      command.mrsValidees.map(m =>
        referentielHabiletesMRS.habiletes(m.codeROME).map(habiletes =>
          MRSAjouteeEvent(
            candidatId = command.id,
            codeROME = m.codeROME,
            departement = m.codeDepartement,
            habiletes = habiletes,
            dateEvaluation = m.dateEvaluation,
            isDHAE = m.isDHAE
          ))
      )
    )
  }

  override def declarerRepriseEmploiParConseiller(context: CandidatContext, command: DeclarerRepriseEmploiParConseillerCommand): List[Event] = {
    require(context.rechercheEmploi.contains(true), s"Le candidat ${command.id.value} n'est pas en recherche d'emploi")

    List(RepriseEmploiDeclareeParConseillerEvent(
      candidatId = command.id,
      conseillerId = command.conseillerId
    ))
  }
}
