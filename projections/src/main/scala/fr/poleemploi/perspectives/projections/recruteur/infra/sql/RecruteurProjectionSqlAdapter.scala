package fr.poleemploi.perspectives.projections.recruteur.infra.sql

import java.time.{ZoneId, ZonedDateTime}

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.recruteur._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurProjectionSqlAdapter(database: Database) {

  import PostgresDriver.api._

  class RecruteurTable(tag: Tag) extends Table[RecruteurRecord](tag, "recruteurs") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def nom = column[Nom]("nom")

    def prenom = column[Prenom]("prenom")

    def email = column[Email]("email")

    def genre = column[Genre]("genre")

    def typeRecruteur = column[Option[TypeRecruteur]]("type_recruteur")

    def raisonSociale = column[Option[String]]("raison_sociale")

    def codePostal = column[Option[String]]("code_postal")

    def commune = column[Option[String]]("commune")

    def pays = column[Option[String]]("pays")

    def numeroSiret = column[Option[NumeroSiret]]("numero_siret")

    def numeroTelephone = column[Option[NumeroTelephone]]("numero_telephone")

    def contactParCandidats = column[Option[Boolean]]("contact_par_candidats")

    def dateInscription = column[ZonedDateTime]("date_inscription")

    def dateDerniereConnexion = column[ZonedDateTime]("date_derniere_connexion")

    def * = (recruteurId, nom, prenom, email, genre, typeRecruteur, raisonSociale, codePostal, commune, pays, numeroSiret, numeroTelephone, contactParCandidats, dateInscription, dateDerniereConnexion) <> (RecruteurRecord.tupled, RecruteurRecord.unapply)
  }

  implicit object RecruteurPourConseillerShape extends CaseClassShape(RecruteurPourConseillerLifted.tupled, RecruteurPourConseillerDto.tupled)

  val recruteurTable = TableQuery[RecruteurTable]
  val typeRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    recruteurTable
      .filter(r => r.recruteurId === recruteurId)
      .map(_.typeRecruteur)
  }
  val profilRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    recruteurTable
      .filter(r => r.recruteurId === recruteurId)
  }
  val modifierProfilQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield (r.typeRecruteur, r.raisonSociale, r.numeroSiret, r.numeroTelephone, r.contactParCandidats)
  }
  val modifierAdresseQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield (r.codePostal, r.commune, r.pays)
  }
  val modifierProfilGerantQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield (r.nom, r.prenom, r.email, r.genre)
  }
  val derniereConnexionQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    for {
      r <- recruteurTable if r.recruteurId === recruteurId
    } yield r.dateDerniereConnexion
  }

  def typeRecruteur(query: TypeRecruteurQuery): Future[TypeRecruteurQueryResult] =
    database.run(typeRecruteurQuery(query.recruteurId).result.head).map(TypeRecruteurQueryResult)

  def profilRecruteur(query: ProfilRecruteurQuery): Future[ProfilRecruteurQueryResult] =
    database.run(profilRecruteurQuery(query.recruteurId).result.head).map(r =>
      ProfilRecruteurQueryResult(
        recruteurId = r.recruteurId,
        typeRecruteur = r.typeRecruteur,
        raisonSociale = r.raisonSociale,
        adresse = for {
          codePostal <- r.codePostal
          commune <- r.commune
          pays <- r.pays
        } yield Adresse(codePostal = codePostal, libelleCommune = commune, libellePays = pays),
        numeroSiret = r.numeroSiret,
        numeroTelephone = r.numeroTelephone,
        contactParCandidats = r.contactParCandidats
      )
    )

  def listerPourConseiller(query: RecruteursPourConseillerQuery): Future[RecruteursPourConseillerQueryResult] = {
    val baseQuery = recruteurTable.filter { r =>
      val filtresChamps = List[Option[Rep[Boolean]]](
        query.dateDebut.map { d =>
          val date = d.atStartOfDay().atZone(ZoneId.systemDefault())
          if (query.rechercheParDateInscription.isEmpty) { // on recherche par date d'inscription ou de derniere connexion
            r.dateInscription >= date || r.dateDerniereConnexion >= date
          } else if (query.rechercheParDateInscription.get) { // recherche seulement par date d'inscription
            r.dateInscription >= date
          } else {
            r.dateDerniereConnexion >= date
          }
        },
        query.dateFin.map { d =>
          val date = d.atStartOfDay().atZone(ZoneId.systemDefault())
          if (query.rechercheParDateInscription.isEmpty) { // on recherche par date d'inscription ou de derniere connexion
            r.dateInscription <= date || r.dateDerniereConnexion <= date
          } else if (query.rechercheParDateInscription.get) { // recherche seulement par date d'inscription
            r.dateInscription <= date
          } else {
            r.dateDerniereConnexion <= date
          }
        }
      )

      val filtresChampsOptionnels = List[Option[Rep[Option[Boolean]]]](
        query.codePostal
          .map(c => r.codePostal === c)
          .orElse(
            if (query.codesDepartement.nonEmpty)
              Some(query.codesDepartement.foldLeft(Some(false): Rep[Option[Boolean]])((acc, codeDepartement) =>
                acc || (r.codePostal like s"${codeDepartement.value}%")
              ))
            else None
          ),
        query.typeRecruteur.map(t => r.typeRecruteur === t),
        query.contactParCandidats.map(c => r.contactParCandidats === c)
      )

      filtresChampsOptionnels.flatten.foldLeft(Some(true): Rep[Option[Boolean]])(_ && _) &&
        filtresChamps.flatten.foldLeft(true: Rep[Boolean])(_ && _)
    }

    for {
      nbRecruteursTotal <- query.page
        .map(_ => Future.successful(0))
        .getOrElse(database.run(baseQuery.length.result))
      recruteurs <- database.run(
        baseQuery
          .filter(r => query.page.map(p => r.dateInscription < p.dateInscription || (r.dateInscription === p.dateInscription && r.recruteurId < p.recruteurId)).getOrElse(true: Rep[Boolean]))
          .sortBy(r => (r.dateInscription.desc, r.recruteurId.desc))
          .take(query.nbRecruteursParPage)
          .map(r => RecruteurPourConseillerLifted(r.recruteurId, r.nom, r.prenom, r.email, r.typeRecruteur, r.raisonSociale, r.commune, r.codePostal, r.contactParCandidats, r.numeroSiret, r.numeroTelephone, r.dateInscription, r.dateDerniereConnexion))
          .result
      ).map(_.toList)
    } yield
      RecruteursPourConseillerQueryResult(
        nbRecruteursTotal = nbRecruteursTotal,
        recruteurs = recruteurs,
        pageSuivante =
          if (recruteurs.size < query.nbRecruteursParPage)
            None
          else
            Some(KeysetRecruteursPourConseiller(
              dateInscription = recruteurs.last.dateInscription,
              recruteurId = recruteurs.last.recruteurId
            ))
      )
  }

  def onRecruteurInscritEvent(event: RecruteurInscritEvent): Future[Unit] =
    database
      .run(recruteurTable.map(r => (r.recruteurId, r.nom, r.prenom, r.genre, r.email, r.dateInscription, r.dateDerniereConnexion))
        += (event.recruteurId, event.nom, event.prenom, event.genre, event.email, event.date, event.date))
      .map(_ => ())

  def onRecruteurConnecteEvent(event: RecruteurConnecteEvent): Future[Unit] =
    database.run(derniereConnexionQuery(event.recruteurId).update(
      event.date
    )).map(_ => ())

  def onProfilModifieEvent(event: ProfilModifieEvent): Future[Unit] =
    database.run(modifierProfilQuery(event.recruteurId).update((
      Some(event.typeRecruteur),
      Some(event.raisonSociale),
      Some(event.numeroSiret),
      Some(event.numeroTelephone),
      Some(event.contactParCandidats)
    ))).map(_ => ())

  def onAdresseModifieeEvent(event: AdresseRecruteurModifieeEvent): Future[Unit] =
    database.run(modifierAdresseQuery(event.recruteurId).update((
      Some(event.adresse.codePostal),
      Some(event.adresse.libelleCommune),
      Some(event.adresse.libellePays)
    ))).map(_ => ())

  def onProfilGerantModifieEvent(event: ProfilGerantModifieEvent): Future[Unit] =
    database.run(modifierProfilGerantQuery(event.recruteurId).update((
      event.nom,
      event.prenom,
      event.email,
      event.genre
    ))).map(_ => ())
}
