package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.ValueObject
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, RayonRecherche}

case class LocalisationRecherche(commune: String,
                                 codePostal: String,
                                 coordonnees: Coordonnees,
                                 rayonRecherche: Option[RayonRecherche]) extends ValueObject
