package conf

import java.nio.file.Paths

import fr.poleemploi.perspectives.candidat.dhae.infra.csv.ImportHabiletesDHAECsvAdapterConfig
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.ImportHabiletesMRSCsvAdapterConfig
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSCandidatPEConnectConfig
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import play.api.Configuration

class BatchsConfig(configuration: Configuration) {

  val useMailjet: Boolean = configuration.getOptional[Boolean]("useMailjet").getOrElse(true)
  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useImportHabiletesMRSCsv: Boolean = configuration.getOptional[Boolean]("useImportHabiletesMRSCsv").getOrElse(true)
  val useImportHabiletesDHAECsv: Boolean = configuration.getOptional[Boolean]("useImportHabiletesDHAECsv").getOrElse(true)

  val version: String = BuildInfo.version
  val webappURL: String = configuration.get[String]("webappURL")

  val partenaireOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "partenaire"
  )

  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = configuration.get[String]("mailjet.sender"),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private"),
    testeurs = configuration.getOptional[Seq[String]]("mailjet.testeurs").map(_.toList).getOrElse(Nil)
  )

  val importMRSCandidatPEConnectConfig: ImportMRSCandidatPEConnectConfig = ImportMRSCandidatPEConnectConfig(
    importDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.candidatsMrsValidees.importDirectory")),
    archiveDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.candidatsMrsValidees.archiveDirectory"))
  )

  val importHabiletesMRSCsvAdapterConfig: ImportHabiletesMRSCsvAdapterConfig = ImportHabiletesMRSCsvAdapterConfig(
    importDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.habiletesMRS.importDirectory")),
    archiveDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.habiletesMRS.archiveDirectory"))
  )

  val importHabiletesDHAECsvAdapterConfig: ImportHabiletesDHAECsvAdapterConfig = ImportHabiletesDHAECsvAdapterConfig(
    importDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.habiletesDHAE.importDirectory")),
    archiveDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.habiletesDHAE.archiveDirectory"))
  )

  val referentielMetierWSAdapterConfig: ReferentielMetierWSAdapterConfig = ReferentielMetierWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi"),
    oauthConfig = partenaireOauthConfig
  )

  val esConfig: EsConfig = EsConfig(
    host = configuration.get[String]("elasticsearch.host"),
    port = configuration.get[Int]("elasticsearch.port")
  )
}
