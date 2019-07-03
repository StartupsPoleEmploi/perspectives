package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite}

import scala.concurrent.Future

// FIXME : split apres avoir déterminer le boundedContext pour la recherche
trait ReferentielMetier {

  def metierParCodeROME(codeROME: CodeROME): Future[Metier]

  def metiersParCodesROME(codesROME: Set[CodeROME]): Future[Set[Metier]]

  def secteursActivitesRecherche: Future[List[SecteurActivite]]

  def secteurActiviteRechercheParCode(codeSecteurActivite: CodeSecteurActivite): Future[SecteurActivite]

  def metiersRechercheParCodeROME(codesROME: Set[CodeROME]): Future[Set[Metier]]
}
