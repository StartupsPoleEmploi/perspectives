package fr.poleemploi.perspectives.recruteur

import java.util.UUID

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain._

import scala.collection.mutable.ListBuffer

class RecruteurBuilder {

  val recruteurId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  private var events: ListBuffer[Event] = ListBuffer()

  def avecInscription(nom: Option[Nom] = None,
                      prenom: Option[Prenom] = None,
                      email: Option[Email] = None,
                      genre: Option[Genre] = None): RecruteurBuilder = {
    events += RecruteurInscritEvent(
      recruteurId = recruteurId,
      nom = nom.getOrElse(Nom("recruteur")),
      prenom = prenom.getOrElse(Prenom("bob")),
      email = email.getOrElse(Email("bob.recruteur@mail.com")),
      genre = genre.getOrElse(Genre.HOMME)
    )
    this
  }

  def avecProfil(typeRecruteur: Option[TypeRecruteur] = None,
                 raisonSociale: Option[String] = None,
                 numeroSiret: Option[NumeroSiret] = None,
                 numeroTelephone: Option[NumeroTelephone] = None,
                 contactParCandidats: Option[Boolean] = None): RecruteurBuilder = {
    events += ProfilModifieEvent(
      recruteurId = recruteurId,
      typeRecruteur = typeRecruteur.getOrElse(TypeRecruteur.ENTREPRISE),
      raisonSociale = raisonSociale.getOrElse("raison sociale"),
      numeroSiret = numeroSiret.getOrElse(NumeroSiret("00000000000018")),
      numeroTelephone = numeroTelephone.getOrElse(NumeroTelephone("0987654356")),
      contactParCandidats = contactParCandidats.getOrElse(true)
    )
    this
  }

  def build: Recruteur = {
    val recruteur = Recruteur(
      id = recruteurId,
      version = events.size,
      state = RecruteurContext().apply(events.toList)
    )
    events = ListBuffer()
    recruteur
  }
}
