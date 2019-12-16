package fr.poleemploi.perspectives.projections.candidat

import java.util.UUID

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, _}
import fr.poleemploi.perspectives.metier.domain.{Metier, SecteurActivite}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.json.{Json, Writes}

import scala.util.Random

case class RechercheCandidatsQuery(typeRecruteur: TypeRecruteur,
                                   utiliserVersionDegradee: Boolean = false,
                                   codeSecteurActivite: Option[CodeSecteurActivite],
                                   codeROME: Option[CodeROME],
                                   coordonnees: Option[Coordonnees],
                                   nbPagesACharger: Int,
                                   page: Option[KeysetCandidatPourRecruteur]) extends Query[RechercheCandidatQueryResult] {
  val nbCandidatsParPage: Int = 10
}

case class CandidatPourRecruteurDto(candidatId: CandidatId,
                                    nom: Nom,
                                    prenom: Prenom,
                                    email: Email,
                                    metiersValides: List[MetierValideDTO],
                                    metiersValidesRecherches: List[Metier],
                                    metiersRecherches: List[Metier],
                                    numeroTelephone: NumeroTelephone,
                                    rayonRecherche: Option[RayonRecherche],
                                    tempsTravailRecherche: Option[TempsTravail],
                                    commune: String,
                                    codePostal: String,
                                    cvId: Option[CVId],
                                    cvTypeMedia: Option[TypeMedia],
                                    centresInteret: List[CentreInteret],
                                    langues: List[Langue],
                                    permis: List[Permis],
                                    savoirEtre: List[SavoirEtre],
                                    savoirFaire: List[SavoirFaire],
                                    formations: List[Formation],
                                    experiencesProfessionnelles: List[ExperienceProfessionnelle]) {

  def nomCV: Option[String] = cvTypeMedia.map(t => s"${prenom.value} ${nom.value}.${TypeMedia.getExtensionFichier(t)}")
}

object CandidatPourRecruteurDto {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[CandidatPourRecruteurDto] = c =>
    Json.writes[CandidatPourRecruteurDto].writes(c) ++ Json.obj(
      "nomCV" -> c.nomCV
    )

  private val random = Random
  private val communes = List("Rennes", "Nantes", "Toulon")
  private val codePostaux = List("35000", "44000", "83000")
  private val savoirsEtre = List(SavoirEtre("autonomie"), SavoirEtre("persévérance"), SavoirEtre("gestion du stress"), SavoirEtre("réactivité"))
  private val habiletes = Set(
    Habilete("Prendre des initiatives et être autonome"),
    Habilete("Recueillir et analyser des données"),
    Habilete("S'adapter au changement"),
    Habilete("Communiquer"),
    Habilete("Se représenter un processus"),
    Habilete("Créer Innover"),
    Habilete("Respecter des normes et des consignes")
  )
  private val metiers = List(
    Metier(codeROME = CodeROME("M1805"), label = "Études et développement informatique"),
    Metier(codeROME = CodeROME("K2503"), label = "Sécurité et surveillance privées"),
    Metier(codeROME = CodeROME("N2203"), label = "Exploitation des pistes aéroportuaires"),
    Metier(codeROME = CodeROME("N1105"), label = "Manutention manuelle de charges")
  )

  def mock(size: Int): List[CandidatPourRecruteurDto] =
    (1 to size).toList.map(mockCandidatPourRecruteurDto)

  private def mockCandidatPourRecruteurDto(index: Int) =
    CandidatPourRecruteurDto(
      candidatId = CandidatId(UUID.randomUUID().toString),
      nom = Nom(s"Nom$index"),
      prenom = Prenom(s"Prenom$index"),
      email = Email(s"nom$index.prenom$index@email.fr"),
      metiersValides = List(MetierValideDTO(
        metier = random.shuffle(metiers).head,
        habiletes = random.shuffle(habiletes).take(random.nextInt(habiletes.size)),
        departement = CodeDepartement(codePostaux.lift(index - 1).getOrElse(codePostaux.head).take(2)),
        isDHAE = false
      )),
      metiersValidesRecherches = Nil,
      metiersRecherches = List(
        Metier(codeROME = CodeROME("N11"), label = "Magasinage, manutention"),
        Metier(codeROME = CodeROME("G11"), label = "Accueil (agent d'accueil, réceptionniste…)")
      ),
      numeroTelephone = NumeroTelephone("0102030405"),
      rayonRecherche = Some(RayonRecherche.MAX_30),
      tempsTravailRecherche = Some(TempsTravail.TEMPS_PLEIN),
      commune = communes.lift(index - 1).getOrElse(communes.head),
      codePostal = codePostaux.lift(index - 1).getOrElse(codePostaux.head),
      cvId = None,
      cvTypeMedia = None,
      centresInteret = Nil,
      langues = List(Langue(label = "Anglais", niveau = Some(NiveauLangue.INTERMEDIAIRE))),
      permis = Nil,
      savoirEtre = random.shuffle(savoirsEtre).take(random.nextInt(savoirsEtre.size)),
      savoirFaire = Nil,
      formations = List(Formation(
        anneeFin = 2019,
        intitule = "BEP CSS",
        lieu = None,
        domaine = Some(DomaineFormation("Santé secteur sanitaire")),
        niveau = Some(NiveauFormation("CAP, BEP ou équivalents"))
      )),
      experiencesProfessionnelles = Nil
    )
}

case class KeysetCandidatPourRecruteur(score: Option[Int],
                                       dateInscription: Long,
                                       candidatId: CandidatId)

object KeysetCandidatPourRecruteur {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[KeysetCandidatPourRecruteur] = Json.writes[KeysetCandidatPourRecruteur]
}

case class RechercheCandidatQueryResult(candidats: List[CandidatPourRecruteurDto],
                                        nbCandidatsTotal: Int,
                                        pagesSuivantes: List[KeysetCandidatPourRecruteur],
                                        metierRecherche: Option[Metier],
                                        secteurRecherche: Option[SecteurActivite]) extends QueryResult

object RechercheCandidatQueryResult {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[RechercheCandidatQueryResult] = Json.writes[RechercheCandidatQueryResult]

  private val MOCK_SIZE = 3

  val mock = RechercheCandidatQueryResult(
    candidats = CandidatPourRecruteurDto.mock(MOCK_SIZE),
    nbCandidatsTotal = MOCK_SIZE,
    pagesSuivantes = Nil,
    metierRecherche = None,
    secteurRecherche = None
  )
}
