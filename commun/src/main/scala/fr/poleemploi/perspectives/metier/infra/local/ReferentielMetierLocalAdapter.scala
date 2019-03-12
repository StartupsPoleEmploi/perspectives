package fr.poleemploi.perspectives.metier.infra.local

import fr.poleemploi.perspectives.commun.domain.{CodeDomaineProfessionnel, CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.metier.domain.{DomaineProfessionnel, Metier, ReferentielMetier, SecteurActivite}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielMetierLocalAdapter extends ReferentielMetier {

  private val secteursActivite = List(
    SecteurActivite(
      code = CodeSecteurActivite("A"),
      label = "Agriculture et espaces verts",
      domainesProfessionnels = List(
        DomaineProfessionnel(
          code = CodeDomaineProfessionnel("A11"),
          label = "Conduite d'engins (tracteurs...)"
        ),
        DomaineProfessionnel(
          code = CodeDomaineProfessionnel("A12"),
          label = "Entretien des Espaces verts (jardinier...)"
        ),
        DomaineProfessionnel(
          code = CodeDomaineProfessionnel("A14"),
          label = "Ouvrier Agricole (fruits, légumes, vin, élevage d'animaux…)"
        ),
        DomaineProfessionnel(
          code = CodeDomaineProfessionnel("A15"),
          label = "Soins aux animaux (soigneur, aide vétérinaire, toilettage...)"
        )
      ),
      metiers = Nil
    ),
    SecteurActivite(
      code = CodeSecteurActivite("D"),
      label = "Commerce",
      domainesProfessionnels = List(
        DomaineProfessionnel(
          code = CodeDomaineProfessionnel("D14"),
          label = "Commercial (vente, téléconseiller…)"
        ),
        DomaineProfessionnel(
          code = CodeDomaineProfessionnel("D15"),
          label = "Grande distribution (caisse, mise en rayon…)"
        )
      ),
      metiers = List(
        Metier(codeROME = CodeROME("D1106"), label = "Alimentation générale (fruits et légumes, produits frais...)"),
        Metier(codeROME = CodeROME("D1101"), label = "Boucherie Charcuterie"),
        Metier(codeROME = CodeROME("D1102"), label = "Boulangerie Patisserie"),
        Metier(codeROME = CodeROME("D1105"), label = "Poissonnerie")
      )
    )
  )

  override def metiersParCodesROME(codesROME: Set[CodeROME]): Future[Set[Metier]] =
    Future.successful(codesROME.map(c => Metier(codeROME = c, label = s"Label métier ${c.value}")))

  override def secteursActivitesRecherche: Future[List[SecteurActivite]] =
    Future.successful(secteursActivite)

  override def secteurActiviteRechercheParCode(codeSecteurActivite: CodeSecteurActivite): Future[SecteurActivite] =
    Future(secteursActivite.find(_.code == codeSecteurActivite)
      .getOrElse(throw new IllegalArgumentException(s"Pas de secteur associé au code ${codeSecteurActivite.value}")))

  override def metiersRechercheParCodeROME(codesROME: Set[CodeROME]): Future[Set[Metier]] =
    Future.successful(codesROME.map(c => Metier(codeROME = c, label = s"Label métier ${c.value}")))
}
