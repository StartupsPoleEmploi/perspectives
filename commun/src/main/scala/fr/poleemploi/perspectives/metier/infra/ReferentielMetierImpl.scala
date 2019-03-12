package fr.poleemploi.perspectives.metier.infra

import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.metier.domain.{Metier, ReferentielMetier, SecteurActivite}
import fr.poleemploi.perspectives.metier.infra.elasticsearch.ReferentielMetierElasticsearchAdapter
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielMetierImpl(elasticsearchAdapter: ReferentielMetierElasticsearchAdapter,
                            wsAdapter: ReferentielMetierWSAdapter) extends ReferentielMetier {

  override def metiersParCodesROME(codesROME: Set[CodeROME]): Future[Set[Metier]] =
    wsAdapter.metiersParCode(codesROME)

  override lazy val secteursActivitesRecherche: Future[List[SecteurActivite]] =
    elasticsearchAdapter.secteursActivites

  override def metiersRechercheParCodeROME(codesROME: Set[CodeROME]): Future[Set[Metier]] =
    secteursActivitesRecherche.map(_.flatMap(_.metiers).filter(m => codesROME.contains(m.codeROME)).toSet)

  override def secteurActiviteRechercheParCode(codeSecteurActivite: CodeSecteurActivite): Future[SecteurActivite] =
    secteursActivitesRecherche.map(_.find(s => codeSecteurActivite == s.code).getOrElse(throw new IllegalArgumentException(s"Aucun secteur d'activité associé au code ${codeSecteurActivite.value}")))
}
