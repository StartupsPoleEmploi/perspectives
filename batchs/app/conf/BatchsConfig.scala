package conf

import java.nio.file.Paths

import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapterConfig
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import play.api.Configuration

class BatchsConfig(configuration: Configuration) {

  val useMailjet: Boolean = configuration.getOptional[Boolean]("useMailjet").getOrElse(true)
  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useImportHabiletesMRS: Boolean = configuration.getOptional[Boolean]("useImportHabiletesMRS").getOrElse(true)

  val version: String = BuildInfo.version

  val partenaireOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "partenaire",
    scopes = Nil
  )
  
  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = Email(configuration.get[String]("mailjet.sender")),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private")
  )
  val mailjetTesteurs: List[Email] =
    configuration.getOptional[Seq[String]]("mailjet.testeurs").map(_.map(Email).toList).getOrElse(Nil)

  val importMRSPEConnectConfig: ImportFileAdapterConfig = ImportFileAdapterConfig(
    importDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.MrsValidees.importDirectory")),
    archiveDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.MrsValidees.archiveDirectory"))
  )

  val importMRSDHAEPEConnectConfig: ImportFileAdapterConfig = ImportFileAdapterConfig(
    importDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.MrsDHAEValidees.importDirectory")),
    archiveDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.MrsDHAEValidees.archiveDirectory"))
  )

  val importHabiletesMRSCsvAdapterConfig: ImportFileAdapterConfig = ImportFileAdapterConfig(
    importDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.habiletesMRS.importDirectory")),
    archiveDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.habiletesMRS.archiveDirectory"))
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
