package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain._

trait ReferentielMetier {

  def habiletesParMetier(code: CodeROME): List[Habilete] =
    ReferentielMetier.habiletesParMetier.getOrElse(code, Nil)

  def metierParCode(code: CodeROME): Metier
}

object ReferentielMetier {

  /** Référentiel en dur obtenu de Mickaël sur les métiers évalués en vendée en 2017 */
  private val habiletesParMetier: Map[CodeROME, List[Habilete]] =
    Map(
      CodeROME("B1802") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.DEXTERITE,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.REPRESENTATION_PROCESSUS
      ),
      CodeROME("D1507") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.ORGANISER,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.TRAVAIL_EN_EQUIPE
      ),
      CodeROME("G1603") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.RELATION_SERVICE,
        Habilete.TRAVAIL_EN_EQUIPE
      ),
      CodeROME("H2102") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.DEXTERITE,
        Habilete.TRAVAIL_EN_EQUIPE
      ),
      CodeROME("H2201") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.DEXTERITE,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.TRAVAIL_EN_EQUIPE
      ),
      CodeROME("H2402") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.DEXTERITE,
        Habilete.MAINTIENT_ATTENTION
      ),
      CodeROME("H2701") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.RECEUIL_ANALYSE_DONNEES,
        Habilete.COMMUNIQUER,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.TRAVAIL_SOUS_TENSION
      ),
      CodeROME("H2903") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.DEXTERITE,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.RECEUIL_ANALYSE_DONNEES
      ),
      CodeROME("H2909") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.DEXTERITE,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.PRISE_D_INITIATIVES
      ),
      CodeROME("H2913") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.DEXTERITE,
        Habilete.REPRESENTATION_ESPACE
      ),
      CodeROME("H3101") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.REPRESENTATION_PROCESSUS,
        Habilete.DEXTERITE,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.TRAVAIL_SOUS_TENSION
      ),
      CodeROME("H3201") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.DEXTERITE,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.REPRESENTATION_PROCESSUS
      ),
      CodeROME("H3203") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.ORGANISER,
        Habilete.RECEUIL_ANALYSE_DONNEES,
        Habilete.TRAVAIL_SOUS_TENSION
      ),
      CodeROME("H3301") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.DEXTERITE,
        Habilete.TRAVAIL_EN_EQUIPE,
        Habilete.TRAVAIL_SOUS_TENSION
      ),
      CodeROME("H3302") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.REPRESENTATION_PROCESSUS,
        Habilete.MAINTIENT_ATTENTION,
        Habilete.TRAVAIL_EN_EQUIPE
      ),
      CodeROME("I1307") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.DEXTERITE,
        Habilete.TRAVAIL_SOUS_TENSION,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.RELATION_SERVICE
      ),
      CodeROME("N1101") -> List(
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.REPRESENTATION_ESPACE,
        Habilete.DEXTERITE,
        Habilete.MAINTIENT_ATTENTION
      ),
      CodeROME("N1103") -> List(
        Habilete.PRISE_D_INITIATIVES,
        Habilete.RESPECT_NORMES_ET_CONSIGNES,
        Habilete.TRAVAIL_SOUS_TENSION
      )
    )
}

