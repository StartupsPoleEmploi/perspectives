package fr.poleemploi.perspectives.recruteur

import java.util.UUID

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte, LocalisationAlerte}

import scala.collection.mutable.ListBuffer

class RecruteurBuilder {

  val recruteurId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  def genererAlerteId: AlerteId = AlerteId(UUID.randomUUID().toString)

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

  def avecAlerte(alerteId: Option[AlerteId] = None,
                 typeRecruteur: Option[TypeRecruteur] = None,
                 email: Option[Email] = None,
                 frequenceAlerte: Option[FrequenceAlerte] = None,
                 codeSecteurActivite: Option[CodeSecteurActivite] = None,
                 codeROME: Option[CodeROME] = None,
                 localisation: Option[LocalisationAlerte] = None): RecruteurBuilder = {
    events += AlerteRecruteurCreeEvent(
      recruteurId = recruteurId,
      typeRecruteur = typeRecruteur.getOrElse(TypeRecruteur.ENTREPRISE),
      email = email.getOrElse(Email("bob.recruteur@mail.com")),
      alerteId = alerteId.getOrElse(genererAlerteId),
      frequence = frequenceAlerte.getOrElse(FrequenceAlerte.HEBDOMADAIRE),
      codeSecteurActivite = codeSecteurActivite.orElse(Some(CodeSecteurActivite("H"))),
      codeROME = codeROME.orElse(Some(CodeROME("H2909"))),
      localisation = localisation.orElse(Some(LocalisationAlerte(
        label = "85",
        coordonnees = Coordonnees(
          latitude = 46.8329,
          longitude = -1.8421
        )
      )))
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
