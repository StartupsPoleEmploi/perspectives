package conf

import akka.actor.{ActorRef, ActorSystem}
import candidat.activite.domain.{EmailingCandidatsJVRService, EmailingDisponibilitesService, ImportOffresEnDifficulteGereesParConseillerService, ImportOffresEnDifficulteGereesParRecruteurService, ImportOffresGereesParConseillerService, ImportOffresGereesParRecruteurService}
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportHabiletesMRS
import fr.poleemploi.perspectives.emailing.domain.ImportProspectService
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers._

class Scheduled @Inject()(batchsScheduler: BatchsScheduler) {
  batchsScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[ImportHabiletesMRSActor](ImportHabiletesMRSActor.name)
    bindActor[ImportProspectsCandidatsActor](ImportProspectsCandidatsActor.name)
    bindActor[EmailingDisponibilitesCandidatActor](EmailingDisponibilitesCandidatActor.name)
    bindActor[EmailingCandidatsJVRActor](EmailingCandidatsJVRActor.name)
    bindActor[ImportOffresGereesParRecruteurActor](ImportOffresGereesParRecruteurActor.name)
    bindActor[ImportOffresEnDifficulteGereesParRecruteurActor](ImportOffresEnDifficulteGereesParRecruteurActor.name)
    bindActor[ImportOffresGereesParConseillerActor](ImportOffresGereesParConseillerActor.name)
    bindActor[ImportOffresEnDifficulteGereesParConseillerActor](ImportOffresEnDifficulteGereesParConseillerActor.name)

    bind(classOf[Scheduled]).asEagerSingleton()
  }

  @Provides
  def importHabiletesMRSActor(importHabiletesMRS: ImportHabiletesMRS): ImportHabiletesMRSActor =
    new ImportHabiletesMRSActor(
      importHabiletesMRS = importHabiletesMRS
    )

  @Provides
  def importProspectsCandidatsActor(importProspectService: ImportProspectService): ImportProspectsCandidatsActor =
    new ImportProspectsCandidatsActor(
      importProspectService = importProspectService
    )

  @Provides
  def importOffresGereesParRecruteur(importOffresGereesParRecruteurService: ImportOffresGereesParRecruteurService): ImportOffresGereesParRecruteurActor =
    new ImportOffresGereesParRecruteurActor(
      importOffresGereesParRecruteurService = importOffresGereesParRecruteurService
    )

  @Provides
  def importOffresEnDifficulteGereesParRecruteurActor(importOffresEnDifficulteGereesParRecruteurService: ImportOffresEnDifficulteGereesParRecruteurService): ImportOffresEnDifficulteGereesParRecruteurActor =
    new ImportOffresEnDifficulteGereesParRecruteurActor(
      importOffresEnDifficulteGereesParRecruteurService = importOffresEnDifficulteGereesParRecruteurService
    )

  @Provides
  def importOffresGereesParConseillerActor(importOffresGereesParConseillerService: ImportOffresGereesParConseillerService): ImportOffresGereesParConseillerActor =
    new ImportOffresGereesParConseillerActor(
      importOffresGereesParConseillerService = importOffresGereesParConseillerService
    )

  @Provides
  def importOffresEnDifficulteGereesParConseillerActor(importOffresEnDifficulteGereesParConseillerService: ImportOffresEnDifficulteGereesParConseillerService): ImportOffresEnDifficulteGereesParConseillerActor =
    new ImportOffresEnDifficulteGereesParConseillerActor(
      importOffresEnDifficulteGereesParConseillerService = importOffresEnDifficulteGereesParConseillerService
    )

  @Provides
  def emailingDisponibilitesCandidatActor(emailingDisponibilitesService: EmailingDisponibilitesService): EmailingDisponibilitesCandidatActor =
    new EmailingDisponibilitesCandidatActor(
      emailingDisponibilitesService = emailingDisponibilitesService
    )

  @Provides
  def emailingCandidatsJVRActor(emailingCandidatsJVRService: EmailingCandidatsJVRService): EmailingCandidatsJVRActor =
    new EmailingCandidatsJVRActor(
      emailingCandidatsJVRService = emailingCandidatsJVRService
    )

  @Provides
  @Singleton
  def batchsScheduler(actorSystem: ActorSystem,
                      @Named(ImportHabiletesMRSActor.name)
                      importHabiletesMRSActor: ActorRef,
                      @Named(ImportProspectsCandidatsActor.name)
                      importProspectsCandidatsActor: ActorRef,
                      @Named(ImportOffresGereesParRecruteurActor.name)
                      importOffresGereesParRecruteurActor: ActorRef,
                      @Named(ImportOffresEnDifficulteGereesParRecruteurActor.name)
                      importOffresEnDifficulteGereesParRecruteurActor: ActorRef,
                      @Named(ImportOffresGereesParConseillerActor.name)
                      importOffresGereesParConseillerActor: ActorRef,
                      @Named(ImportOffresEnDifficulteGereesParConseillerActor.name)
                      importOffresEnDifficulteGereesParConseillerActor: ActorRef,
                      @Named(EmailingDisponibilitesCandidatActor.name)
                      emailingDisponibilitesCandidatActor: ActorRef,
                      @Named(EmailingCandidatsJVRActor.name)
                      emailingCandidatsJVRActor: ActorRef): BatchsScheduler =
    new BatchsScheduler(
      actorSystem = actorSystem,
      importHabiletesMRSActor = importHabiletesMRSActor,
      importProspectsCandidatsActor = importProspectsCandidatsActor,
      importOffresGereesParRecruteurActor = importOffresGereesParRecruteurActor,
      importOffresEnDifficulteGereesParRecruteurActor = importOffresEnDifficulteGereesParRecruteurActor,
      importOffresGereesParConseillerActor = importOffresGereesParConseillerActor,
      importOffresEnDifficulteGereesParConseillerActor = importOffresEnDifficulteGereesParConseillerActor,
      emailingDisponibilitesCandidatActor = emailingDisponibilitesCandidatActor,
      emailingCandidatsJVRActor = emailingCandidatsJVRActor
    )
}
