package fr.poleemploi.perspectives.recruteur

import java.util.UUID

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}

import scala.collection.mutable.ListBuffer

class RecruteurBuilder {

  val recruteurId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  private var events: ListBuffer[Event] = ListBuffer()

  def avecInscription(nom: Option[String] = None,
                      prenom: Option[String] = None,
                      email: Option[Email] = None,
                      genre: Option[Genre] = None): RecruteurBuilder = {
    events += RecruteurInscritEvent(
      recruteurId = recruteurId,
      nom = nom.getOrElse("recruteur"),
      prenom = prenom.getOrElse("bob"),
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
    val recruteur = new Recruteur(
      id = recruteurId,
      version = events.size,
      events = events.toList
    )
    events = ListBuffer()
    recruteur
  }
}
